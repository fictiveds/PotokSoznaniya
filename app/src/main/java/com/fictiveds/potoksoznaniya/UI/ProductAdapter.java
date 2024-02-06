package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fictiveds.potoksoznaniya.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = new ArrayList<>(productList);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductDescription.setText(product.getDescription());
        holder.textViewProductPrice.setText(String.valueOf(product.getPrice()));
        // Изображение больше не отображается
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void addProduct(Product product) {
        productList.add(product);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView textViewProductName, textViewProductDescription, textViewProductPrice;
        MaterialButton buttonDeleteProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            buttonDeleteProduct = itemView.findViewById(R.id.buttonDeleteProduct);
            // Обработчик клика для buttonDeleteProduct, если нужен
        }
    }
}
