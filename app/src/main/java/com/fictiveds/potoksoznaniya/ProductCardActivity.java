package com.fictiveds.potoksoznaniya;

import static java.util.Locale.filter;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fictiveds.potoksoznaniya.UI.Product;
import com.fictiveds.potoksoznaniya.UI.ProductAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ProductCardActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductPrice;
    private ImageView imageViewProductImage;
    private MaterialButton buttonAddProduct, buttonChooseImage;
    private Uri imageUri;
    private DatabaseReference mDatabaseRef;
    private List<Product> productListFull;
    private StorageReference mStorageRef;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();


    private ChildEventListener mChildEventListener;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(imageViewProductImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_card);

     productList = new ArrayList<>();
     productListFull = new ArrayList<>();



        // Инициализация UI компонентов
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        productAdapter = new ProductAdapter(this, productList);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewProducts.setAdapter(productAdapter);

        // Инициализация Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("products");
        mStorageRef = FirebaseStorage.getInstance().getReference("product_images");

        attachDatabaseReadListener();

        buttonChooseImage.setOnClickListener(v -> chooseImage());
        buttonAddProduct.setOnClickListener(v -> addProductToDatabase());

        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteProduct(position);
            }
        });

        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText); // Вызов фильтра адаптера с новым текстом
                return true;
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void addProductToDatabase() {
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();
        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
        String placeholderImageUri = "android.resource://" + getPackageName() + "/" + R.drawable.ic_placeholder_image;

        if (!name.isEmpty() && !description.isEmpty()) {
            final String productId = mDatabaseRef.push().getKey();

            Product newProduct = new Product(productId, name, description, price, placeholderImageUri);
            productAdapter.addProduct(newProduct);
            if (imageUri != null) {
                StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
                fileRef.putFile(imageUri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri != null ? downloadUri.toString() : null;
                        Product product = new Product(productId, name, description, price, imageUrl);
                        saveProductToDatabase(product);
                    } else {
                        Toast.makeText(this, R.string.upload_failed + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Если изображение не было выбрано, используем URI изображения-заглушки из ресурсов
              //  String placeholderImageUri = "android.resource://" + getPackageName() + "/" + R.drawable.ic_placeholder_image;
                Product product = new Product(productId, name, description, price, placeholderImageUri);
                saveProductToDatabase(product);
            }
        } else {
            Toast.makeText(this, R.string.name_description_empty, Toast.LENGTH_SHORT).show();
        }
        Log.d("ProductAdd", "Product added to database: " + name);
    }

    private void saveProductToDatabase(Product product) {
        mDatabaseRef.child(product.getId()).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productAdapter.addProduct(product);
                Toast.makeText(ProductCardActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProductCardActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachDatabaseReadListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        if (productList != null && productListFull != null) {
                            productList.add(product);
                            productListFull.add(product); // Обновляем полный список
                            Log.d("FirebaseData", "Product added: " + product.getName());
                            productAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("FirebaseData", "onChildChanged called");
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        int productListIndex = -1;
                        int productListFullIndex = -1;
                        for (int i = 0; i < productList.size(); i++) {
                            if (productList.get(i).getId().equals(product.getId())) {
                                productListIndex = i;
                                productList.set(i, product);
                                break;
                            }
                        }
                        for (int i = 0; i < productListFull.size(); i++) {
                            if (productListFull.get(i).getId().equals(product.getId())) {
                                productListFullIndex = i;
                                productListFull.set(i, product);
                                break;
                            }
                        }
                        if (productListIndex != -1) {
                            productAdapter.notifyItemChanged(productListIndex);
                        }
                    }
                }


                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    // ...
                    Log.d("FirebaseData", "onChildRemoved called");
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // ...
                    Log.d("FirebaseData", "onChildMoved called");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ProductCardActivity", "Database error", databaseError.toException());
                }
            };
            mDatabaseRef.addChildEventListener(mChildEventListener);
        }
    }

    private boolean productListContains(Product product) {
        for (Product p : productList) {
            if (p.getId().equals(product.getId())) {
                return true;
            }
        }
        return false;
    }
    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mDatabaseRef.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            productList.clear();
        }
    }


    private void deleteProduct(int position) {
        Product productToRemove = productList.get(position);
        String productId = productToRemove.getId();
        if (productId != null) {
            mDatabaseRef.child(productId).removeValue().addOnSuccessListener(aVoid -> {
                productList.remove(position);
                productListFull.remove(productToRemove); // Удаляем из полного списка
                productAdapter.notifyItemRemoved(position);
                Toast.makeText(ProductCardActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ProductCardActivity.this, "Failed to delete product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e("ProductCardActivity", "Product ID is null.");
        }
        Log.d("ProductDelete", "Product removed from database: " + productId);
    }



}
