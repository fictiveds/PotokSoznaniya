package com.fictiveds.potoksoznaniya;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fictiveds.potoksoznaniya.R;
import com.fictiveds.potoksoznaniya.UI.Product;
import com.fictiveds.potoksoznaniya.UI.ProductAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProductCardActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductPrice;
    private MaterialButton buttonAddProduct, buttonChooseImage;
    private DatabaseReference mDatabaseRef;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_card);

        // Инициализация UI компонентов
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonChooseImage = findViewById(R.id.buttonChooseImage); // Можно убрать, если в UI это тоже удалено

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, productList);
        recyclerViewProducts.setAdapter(productAdapter);

        // Инициализация Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("products");

        buttonAddProduct.setOnClickListener(v -> addProductToDatabase());
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {
                    productList.add(product);
                    productAdapter.notifyItemInserted(productList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                // Обновление товара, если есть изменения
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {
                    int indexToUpdate = -1;
                    for (int i = 0; i < productList.size(); i++) {
                        if (productList.get(i).getId().equals(product.getId())) {
                            indexToUpdate = i;
                            break;
                        }
                    }
                    if (indexToUpdate != -1) {
                        productList.set(indexToUpdate, product);
                        productAdapter.notifyItemChanged(indexToUpdate);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Удаление товара из списка
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {
                    int indexToRemove = -1;
                    for (int i = 0; i < productList.size(); i++) {
                        if (productList.get(i).getId().equals(product.getId())) {
                            indexToRemove = i;
                            break;
                        }
                    }
                    if (indexToRemove != -1) {
                        productList.remove(indexToRemove);
                        productAdapter.notifyItemRemoved(indexToRemove);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                // Это событие происходит, если один из товаров переместился в списке
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Это событие происходит в случае ошибки
                Toast.makeText(ProductCardActivity.this, "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabaseRef.addChildEventListener(childEventListener);
    }


    private void setupDeleteButton(ProductAdapter.ProductViewHolder holder, final int position) {
        holder.buttonDeleteProduct.setOnClickListener(v -> {
            Product product = productList.get(position);
            String productId = product.getId();
            if (productId != null) {
                mDatabaseRef.child(productId).removeValue().addOnSuccessListener(aVoid -> {
                    productAdapter.removeProduct(position);
                    Toast.makeText(ProductCardActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ProductCardActivity.this, "Failed to delete product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void addProductToDatabase() {
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();
        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

        if (!name.isEmpty() && !description.isEmpty()) {
            final String productId = mDatabaseRef.push().getKey();
            Product product = new Product(productId, name, description, price); // imageUrl передаем null
            saveProductToDatabase(product);
        } else {
            Toast.makeText(this, "Name and description cannot be empty.", Toast.LENGTH_SHORT).show();
        }
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
}
