package com.fictiveds.potoksoznaniya;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fictiveds.potoksoznaniya.UI.Product;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;

public class ProductCardActivity extends AppCompatActivity {
    private TextInputEditText editTextProductName, editTextProductDescription, editTextProductPrice;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter adapter;
    private DatabaseReference databaseProducts;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_card);

        // Инициализация Firebase
        databaseProducts = FirebaseDatabase.getInstance().getReference("products");

        // Инициализация UI компонентов
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, databaseProducts);

        recyclerViewProducts.setHasFixedSize(true);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Кнопка добавления продукта
        findViewById(R.id.buttonAddProduct).setOnClickListener(view -> addProduct());

        // Слушатель изменений в базе данных
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> newProducts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    newProducts.add(product);
                }
                adapter.updateProductList(newProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductCardActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void addProduct() {
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceString = editTextProductPrice.getText().toString().trim();
        if (!name.isEmpty() && !description.isEmpty() && !priceString.isEmpty()) {
            double price = Double.parseDouble(priceString);
            String id = databaseProducts.push().getKey();

            Product product = new Product(id, name, description, price);
            if (id != null) {
                databaseProducts.child(id).setValue(product);
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                editTextProductName.setText("");
                editTextProductDescription.setText("");
                editTextProductPrice.setText("");
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
