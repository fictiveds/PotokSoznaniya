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

        // Кнопка добавления продукта
        findViewById(R.id.buttonAddProduct).setOnClickListener(view -> addProduct());

        // Слушатель изменений в базе данных
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
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
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
