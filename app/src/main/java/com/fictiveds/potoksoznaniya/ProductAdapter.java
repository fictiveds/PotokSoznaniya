package com.fictiveds.potoksoznaniya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fictiveds.potoksoznaniya.UI.Product;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context mCtx;
    private List<Product> productList;
    private DatabaseReference databaseReference;

    public ProductAdapter(Context mCtx, List<Product> productList, DatabaseReference databaseReference) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductDescription.setText(product.getDescription());
        holder.textViewProductPrice.setText(String.valueOf(product.getPrice()));

        holder.buttonDeleteProduct.setOnClickListener(view -> {
            databaseReference.child(product.getId()).removeValue();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewProductDescription, textViewProductPrice;
        MaterialButton buttonDeleteProduct;

        public ProductViewHolder(View itemView) {
            super(itemView);

            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            buttonDeleteProduct = itemView.findViewById(R.id.buttonDeleteProduct);
        }
    }
}
