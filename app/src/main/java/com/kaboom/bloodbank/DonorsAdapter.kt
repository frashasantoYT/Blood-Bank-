import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kaboom.bloodbank.db.Users
import com.kaboom.bloodbank.databinding.DonorListBinding
import com.kaboom.bloodbank.db.DonorDB

class DonorsAdapter(private val context: Context, private val donorList: ArrayList<DonorDB>) : RecyclerView.Adapter<DonorsAdapter.DonorsViewHolder>() {
    private var filteredDonorList: MutableList<DonorDB> = donorList.toMutableList()

    fun setFilteredList(filteredList: List<DonorDB>) {
        filteredDonorList.clear()
        filteredDonorList.addAll(filteredList)
        notifyDataSetChanged()
    }
    inner class DonorsViewHolder(val adapterBinding: DonorListBinding) : RecyclerView.ViewHolder(adapterBinding.root) {
        // Initialize the btnContact button from the layout
        val btnContact = adapterBinding.btnContact

        init {
            btnContact.setOnClickListener {
                val phoneNumber = donorList[adapterPosition].phoneNumber
                openDialer(phoneNumber)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorsViewHolder {
        val binding = DonorListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return donorList.size
    }

    override fun onBindViewHolder(holder: DonorsViewHolder, position: Int) {
        holder.adapterBinding.donorName.text = "Name: ${donorList[position].fullName}"
        holder.adapterBinding.donorContact.text = donorList[position].phoneNumber
        holder.adapterBinding.donorAddress.text = "Address ${donorList[position].address}"
        holder.adapterBinding.donorBloodGroup.text = donorList[position].bloodGroup
    }

    private fun openDialer(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))

        // Check if the device has the necessary app to handle the intent
        if (dialIntent.resolveActivity(context.packageManager) != null) {
            // Start the dialer activity
            context.startActivity(dialIntent)
        } else {
            // If no dialer app is available, you can show a toast or handle the situation
            Toast.makeText(context, "No dialer app found.", Toast.LENGTH_SHORT).show()
        }
    }
}
