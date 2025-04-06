package com.nibm.mynewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var serviceCategoriesRecycler: RecyclerView
    private lateinit var categoryAdapter: ServiceCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        serviceCategoriesRecycler = view.findViewById(R.id.service_categories_recycler)
        setupServiceCategories()

        return view
    }

    private fun setupServiceCategories() {
        val categories = listOf(
            ServiceCategory("Plumbing", R.drawable.ic_plumber),
            ServiceCategory("Electrical", R.drawable.ic_electrician),
            ServiceCategory("Cleaning", R.drawable.ic_cleaning),
            ServiceCategory("Carpentry", R.drawable.ic_carpenter),
            ServiceCategory("Painting", R.drawable.ic_painting),
            ServiceCategory("Gardening", R.drawable.ic_gardener)
        )
        categoryAdapter = ServiceCategoryAdapter(categories) { category ->
            when (category.name) {
                "Plumbing" -> startActivity(android.content.Intent(requireContext(), PlumberActivity::class.java))
                //"Painting" -> startActivity(android.content.Intent(requireContext(), PainterActivity::class.java))
                //"Gardening" -> startActivity(android.content.Intent(requireContext(), GardenerActivity::class.java))
                else -> android.widget.Toast.makeText(requireContext(), "${category.name} coming soon!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        serviceCategoriesRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }
}

// Data class for service categories
data class ServiceCategory(val name: String, val iconResId: Int)

// RecyclerView Adapter for service categories
class ServiceCategoryAdapter(
    private val categories: List<ServiceCategory>,
    private val onItemClick: (ServiceCategory) -> Unit
) : RecyclerView.Adapter<ServiceCategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: android.widget.ImageView = itemView.findViewById(R.id.category_icon)
        val name: android.widget.TextView = itemView.findViewById(R.id.category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.icon.setImageResource(category.iconResId)
        holder.name.text = category.name
        holder.itemView.setOnClickListener { onItemClick(category) }
    }

    override fun getItemCount(): Int = categories.size
}

// Grid spacing decoration
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) outRect.top = spacing
        }
    }
}