package com.nibm.mynewapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

class PlumberActivity : AppCompatActivity() {
    private lateinit var searchInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlumberAdapter
    private val plumberList = mutableListOf<Plumber>()
    private val originalList = mutableListOf<Plumber>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plumber)

        // Initialize views
        searchInput = findViewById(R.id.search_input)
        recyclerView = findViewById(R.id.plumbers_recycler_view)

        // Setup default plumbers
        initializePlumbers()

        // Setup RecyclerView
        adapter = PlumberAdapter(plumberList) { plumber ->
            // Handle click event
            val intent = Intent(this, PlumberDetailsActivity::class.java)
            intent.putExtra("PLUMBER_DATA", plumber)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Add search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString()
                performSearch(searchQuery)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun initializePlumbers() {
        // Default plumbers with custom image resources, intro, and contact details
        plumberList.add(Plumber(
            name = "John Smith",
            specialty = "Residential Plumbing",
            imageResId = R.drawable.john_smith,
            rating = 4.5f,
            intro = "John has 10 years of experience in residential plumbing, specializing in pipe repairs and installations.",
            phone = "+1-555-0123",
            email = "john.smith@plumbingservice.com"
        ))
        plumberList.add(Plumber(
            name = "Mike Johnson",
            specialty = "Emergency Repairs",
            imageResId = R.drawable.mike_johnson,
            rating = 4.0f,
            intro = "Mike is your go-to expert for 24/7 emergency plumbing services with quick response times.",
            phone = "+1-555-0456",
            email = "mike.johnson@plumbingservice.com"
        ))
        plumberList.add(Plumber(
            name = "Sarah Williams",
            specialty = "Pipe Installation",
            imageResId = R.drawable.sarah_williams,
            rating = 4.8f,
            intro = "Sarah excels in new construction plumbing and complex pipe installation projects.",
            phone = "+1-555-0789",
            email = "sarah.williams@plumbingservice.com"
        ))
        plumberList.add(Plumber(
            name = "Robert Brown",
            specialty = "Leak Detection",
            imageResId = R.drawable.robert_brown,
            rating = 4.2f,
            intro = "Robert uses advanced technology to detect and fix leaks efficiently.",
            phone = "+1-555-0101",
            email = "robert.brown@plumbingservice.com"
        ))
        plumberList.add(Plumber(
            name = "Emma Davis",
            specialty = "Water Heater Specialist",
            imageResId = R.drawable.emma_davis,
            rating = 4.7f,
            intro = "Emma specializes in water heater installation, repair, and maintenance.",
            phone = "+1-555-0134",
            email = "emma.davis@plumbingservice.com"
        ))

        // Keep a copy of original list for search filtering
        originalList.addAll(plumberList)
    }

    private fun performSearch(query: String) {
        plumberList.clear()
        if (query.isEmpty()) {
            plumberList.addAll(originalList)
        } else {
            val filteredList = originalList.filter {
                it.name.contains(query, ignoreCase = true)
            }
            plumberList.addAll(filteredList)
        }
        adapter.notifyDataSetChanged()

        if (plumberList.isEmpty()) {
            Toast.makeText(this, "No plumbers found for: $query", Toast.LENGTH_SHORT).show()
        }
    }
}

class PlumberAdapter(
    private val plumbers: List<Plumber>,
    private val onItemClick: (Plumber) -> Unit
) : RecyclerView.Adapter<PlumberAdapter.PlumberViewHolder>() {

    class PlumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.plumber_image)
        val nameTextView: TextView = itemView.findViewById(R.id.plumber_name)
        val specialtyTextView: TextView = itemView.findViewById(R.id.plumber_specialty)
        val ratingBar: RatingBar = itemView.findViewById(R.id.plumber_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlumberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plumber, parent, false)
        return PlumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlumberViewHolder, position: Int) {
        val plumber = plumbers[position]
        holder.imageView.setImageResource(plumber.imageResId)
        holder.nameTextView.text = plumber.name
        holder.specialtyTextView.text = plumber.specialty
        holder.ratingBar.rating = plumber.rating

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(plumber)
        }
    }

    override fun getItemCount(): Int = plumbers.size
}