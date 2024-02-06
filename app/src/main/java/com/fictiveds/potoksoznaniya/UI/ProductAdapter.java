package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fictiveds.potoksoznaniya.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private List<Product> productList; // Сделаем его не final, чтобы можно было изменять
     private List<Product> productListFull; // Копия списка для фильтрации

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        if (productList != null) {
            this.productList = productList;
            this.productListFull = new ArrayList<>(productList);
        } else {
            this.productList = new ArrayList<>();
            this.productListFull = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductDescription.setText(product.getDescription());
        holder.textViewProductPrice.setText(String.valueOf(product.getPrice()));
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .into(holder.imageViewProductImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            Log.d("Filter", "ProductListFull size before filtering: " + productListFull.size());

            // Логирование для проверки актуального состояния productListFull перед фильтрацией
            for (Product product : productListFull) {
                Log.d("Filter", "Product in full list: Name - " + product.getName() + ", Description - " + product.getDescription());
            }

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                Log.d("Filter", "Search query: " + filterPattern); // Логирование поискового запроса

                for (Product item : productListFull) {
                    // Условие фильтрации
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                        // Логирование продуктов, соответствующих условию фильтрации
                        Log.d("Filter", "Filtered product: Name - " + item.getName() + ", Description - " + item.getDescription());
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            Log.d("Filter", "Filtered list size after filtering: " + filteredList.size()); // Логирование размера отфильтрованного списка после фильтрации

            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List) results.values);
            Log.d("Filter", "PublishResults: " + productList.size());
            notifyDataSetChanged();
        }
    };


    public void addProduct(Product product) {
        productList.add(product);
        productListFull.add(product); // Добавляем продукт также в полный список для фильтрации
        notifyDataSetChanged();
    }

    public void updateProductList(List<Product> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        productListFull.clear();
        productListFull.addAll(newProductList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView textViewProductName, textViewProductDescription, textViewProductPrice;
        ImageView imageViewProductImage;
        MaterialButton buttonDeleteProduct;

        public ProductViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            imageViewProductImage = itemView.findViewById(R.id.imageViewProduct);
            buttonDeleteProduct = itemView.findViewById(R.id.buttonDeleteProduct);

            buttonDeleteProduct.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }
}

