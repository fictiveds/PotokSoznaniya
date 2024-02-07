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
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private Context mCtx;
    private List<Product> productList;
    private List<Product> productListFull;
    private DatabaseReference databaseReference;

    public ProductAdapter(Context mCtx, List<Product> productList, DatabaseReference databaseReference) {
        this.mCtx = mCtx;
        this.productList = productList;
        productListFull = new ArrayList<>(productList);
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

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Product item : productListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateProductList(List<Product> newProducts) {
        productList.clear();
        productList.addAll(newProducts);
        productListFull.clear();
        productListFull.addAll(newProducts); // Обновляем полный список
        notifyDataSetChanged();
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
