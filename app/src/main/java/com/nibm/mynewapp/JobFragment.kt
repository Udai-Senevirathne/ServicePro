package com.nibm.mynewapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class JobFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var promoCodeInput: EditText
    private lateinit var applyButton: Button
    private lateinit var clearButton: Button
    private lateinit var statusText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var promoAdapter: PromotionsAdapter
    private lateinit var historyAdapter: PromoHistoryAdapter
    private lateinit var database: DatabaseReference
    private val promotionsList = mutableListOf<Promotion>()
    private val appliedPromoCodes = mutableListOf<String>()

    companion object {
        const val TAG = "JobFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_job, container, false)
            ?: throw IllegalStateException("Fragment Job layout could not be inflated")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated called")

        // Set background color to pastel blue
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_blue))

        // Initialize Firebase Database
        try {
            database = FirebaseDatabase.getInstance().reference
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}")
            Toast.makeText(context, "Firebase initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Initialize views with null checks
        recyclerView = view.findViewById(R.id.rvPromotions)
            ?: run { showError("RecyclerView rvPromotions not found"); return }
        promoCodeInput = view.findViewById(R.id.etPromoCode)
            ?: run { showError("EditText etPromoCode not found"); return }
        applyButton = view.findViewById(R.id.btnApplyPromo)
            ?: run { showError("Button btnApplyPromo not found"); return }
        clearButton = view.findViewById(R.id.btnClearPromo)
            ?: run { showError("Button btnClearPromo not found"); return }
        statusText = view.findViewById(R.id.tvStatus)
            ?: run { showError("TextView tvStatus not found"); return }
        historyRecyclerView = view.findViewById(R.id.rvPromoHistory)
            ?: run { showError("RecyclerView rvPromoHistory not found"); return }

        // Set up Promotions RecyclerView
        promoAdapter = PromotionsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = promoAdapter
        Log.d(TAG, "Promotions RecyclerView initialized")

        // Set up Promo History RecyclerView
        historyAdapter = PromoHistoryAdapter()
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyRecyclerView.adapter = historyAdapter
        Log.d(TAG, "History RecyclerView initialized")

        // Load data from Firebase (or initialize sample data if Firebase fails)
        loadPromotionsFromFirebase()
        loadPromoHistoryFromFirebase()

        // Apply button click listener
        applyButton.setOnClickListener {
            val code = promoCodeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                val isValid = promotionsList.any { it.promoCode == code }
                if (isValid) {
                    statusText.text = "Success: $code applied!"
                    statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    if (!appliedPromoCodes.contains(code)) {
                        appliedPromoCodes.add(code)
                        savePromoHistoryToFirebase()
                        historyAdapter.updateHistory(appliedPromoCodes)
                    }
                } else {
                    statusText.text = "Error: Invalid promo code"
                    statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                }
                Toast.makeText(context, statusText.text, Toast.LENGTH_SHORT).show()
            } else {
                statusText.text = "Please enter a promo code"
                statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                Toast.makeText(context, "Please enter a promo code", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear button click listener
        clearButton.setOnClickListener {
            promoCodeInput.text.clear()
            statusText.text = "Ready to apply a promo code"
            statusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun loadPromotionsFromFirebase() {
        database.child("promotions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                promotionsList.clear()
                Log.d(TAG, "Loading promotions from Firebase")
                for (data in snapshot.children) {
                    val promo = data.getValue(Promotion::class.java)
                    promo?.let {
                        promotionsList.add(it)
                        Log.d(TAG, "Added promotion: ${it.promoCode}")
                    } ?: Log.w(TAG, "Failed to parse promotion: ${data.key}")
                }
                Log.d(TAG, "Promotions list size: ${promotionsList.size}")
                promoAdapter.updatePromotions(promotionsList)
                if (promotionsList.isEmpty()) {
                    Log.d(TAG, "No promotions found in Firebase, initializing sample data")
                    initializeSamplePromotions()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load promotions: ${error.message}")
                Toast.makeText(context, "Failed to load promotions: ${error.message}", Toast.LENGTH_SHORT).show()
                // Fallback to sample data if Firebase fails
                initializeSamplePromotions()
            }
        })
    }

    private fun loadPromoHistoryFromFirebase() {
        database.child("promo_history").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                appliedPromoCodes.clear()
                for (data in snapshot.children) {
                    val code = data.getValue(String::class.java)
                    code?.let {
                        appliedPromoCodes.add(it)
                        Log.d(TAG, "Added promo history: $it")
                    }
                }
                Log.d(TAG, "Promo history size: ${appliedPromoCodes.size}")
                historyAdapter.updateHistory(appliedPromoCodes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load history: ${error.message}")
                Toast.makeText(context, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initializeSamplePromotions() {
        val samplePromos = listOf(
            Promotion(10, "10% off first service", "2025-06-30", "WELCOME10"),
            Promotion(25, "Holiday Special", "2025-12-31", "HOLIDAY25"),
            Promotion(15, "Weekend Deal", "2025-04-15", "WEEKEND15")
        )
        promotionsList.clear() // Clear to avoid duplicates
        promotionsList.addAll(samplePromos)
        Log.d(TAG, "Initialized sample promotions, size: ${promotionsList.size}")
        promoAdapter.updatePromotions(promotionsList)
        savePromotionsToFirebase()
    }

    private fun savePromotionsToFirebase() {
        val promotionsMap = promotionsList.associateBy { it.promoCode }
        database.child("promotions").setValue(promotionsMap)
            .addOnSuccessListener {
                Log.d(TAG, "Promotions saved to Firebase successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save promotions: ${it.message}")
                Toast.makeText(context, "Failed to save promotions: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePromoHistoryToFirebase() {
        database.child("promo_history").setValue(appliedPromoCodes)
            .addOnSuccessListener {
                Log.d(TAG, "Promo history saved to Firebase successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save promo history: ${it.message}")
                Toast.makeText(context, "Failed to save promo history: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Data class for Promotion
data class Promotion(
    val discountPercentage: Int = 0,
    val description: String = "",
    val expiryDate: String = "",
    val promoCode: String = ""
)

// Promotions RecyclerView Adapter
class PromotionsAdapter : RecyclerView.Adapter<PromotionsAdapter.PromoViewHolder>() {
    private var promotions: List<Promotion> = emptyList()

    fun updatePromotions(newPromotions: List<Promotion>) {
        promotions = newPromotions
        Log.d(JobFragment.TAG, "Updating promotions adapter with ${promotions.size} items")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotion, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val promo = promotions[position]
        holder.title.text = "${promo.discountPercentage}% Off - ${promo.description}"
        holder.details.text = "Expires: ${promo.expiryDate}"
        holder.promoCode.text = "Code: ${promo.promoCode}"
        Log.d(JobFragment.TAG, "Binding promotion: ${promo.promoCode}")
    }

    override fun getItemCount(): Int {
        Log.d(JobFragment.TAG, "Promotions adapter item count: ${promotions.size}")
        return promotions.size
    }

    class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvPromoTitle)
        val details: TextView = itemView.findViewById(R.id.tvPromoDetails)
        val promoCode: TextView = itemView.findViewById(R.id.tvPromoCode)
    }
}

// Promo History RecyclerView Adapter
class PromoHistoryAdapter : RecyclerView.Adapter<PromoHistoryAdapter.HistoryViewHolder>() {
    private var history: List<String> = emptyList()

    fun updateHistory(newHistory: List<String>) {
        history = newHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.textView.text = history[position]
    }

    override fun getItemCount(): Int = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}