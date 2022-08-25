package naya.ganj.app.data.mycart.view

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddAddressViewModel
import naya.ganj.app.databinding.ActivityAddAddressBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.Utility

class AddAddressActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddAddressBinding
    lateinit var viewModel: AddAddressViewModel
    private var addressType = ""
    private var addressId = ""
    var isUpdateAddress = false
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        if (app.user.getAppLanguage() == 1) {
            binding.tvPersonelDetail.text = resources.getString(R.string.personal_details_h)
            binding.textView9.text = resources.getString(R.string.address_details_h)
            binding.tvFirstName.hint = resources.getString(R.string.enter_first_name_h)
            binding.tvLastName.hint = resources.getString(R.string.enter_last_name_h)
            binding.tvMobile.hint = resources.getString(R.string.mobile_number_h)
            binding.tvHouse.hint = resources.getString(R.string.house_no_h)
            binding.tvApart.hint = resources.getString(R.string.apart_name_h)
            binding.tvStreet.hint = resources.getString(R.string.street_details_h)
            binding.tvAddress.hint = resources.getString(R.string.area_details_h)
            binding.tvCity.hint = resources.getString(R.string.city_h)
            binding.tvPin.hint = resources.getString(R.string.pincode_h)
            binding.tvSelectAddress.hint = resources.getString(R.string.select_address_type_h)
        }

        if (intent.extras != null) {
            isUpdateAddress = true
            if(app.user.getAppLanguage()==1){
                binding.btnAddAddress.text = resources.getString(R.string.update_address_h)
            }else{
                binding.btnAddAddress.text = "Update Address"
            }

            binding.include2.toolbarTitle.text = "Edit Address"

            binding.tvFirstName.editText?.setText(intent.getStringExtra("firstName"))
            binding.tvLastName.editText?.setText(intent.getStringExtra("lastName"))
            binding.tvMobile.editText?.setText(intent.getStringExtra("contactNumber"))
            binding.tvHouse.editText?.setText(intent.getStringExtra("houseNo"))
            binding.tvApart.editText?.setText(intent.getStringExtra("ApartName"))
            binding.tvStreet.editText?.setText(intent.getStringExtra("street"))
            binding.tvAddress.editText?.setText(intent.getStringExtra("landmark"))
            binding.tvCity.editText?.setText(intent.getStringExtra("city"))
            binding.tvPin.editText?.setText(intent.getStringExtra("pincode"))
            binding.tvSelectAddress.editText?.setText(intent.getStringExtra("nickName"))

            addressId = intent.getStringExtra("addressId").toString()
            addressType = intent.getStringExtra("nickName").toString()

        } else {

            if(app.user.getAppLanguage()==1){
                binding.btnAddAddress.text =resources.getString(R.string.add_address_h)
                binding.include2.toolbarTitle.text = resources.getString(R.string.add_new_address_h)
            }else{
                binding.btnAddAddress.text = "Add Address"
                binding.include2.toolbarTitle.text = "Add New Address"
            }
        }

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(AddAddressViewModel::class.java)

        binding.include2.ivBackArrow.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right)}
        
        val addressList =
            listOf("Home(7 AM - 9 PM delivery)", "Office/Commercial(10 AM - 6 PM delivery)")
        val addressAdapter =
            ArrayAdapter(this@AddAddressActivity, R.layout.auto_list_item, addressList)
        binding.autoCompleteTextView.setAdapter(addressAdapter)


        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            addressType = addressAdapter.getItem(position).toString()
        }

        //TODO changes in the city
        /*val cityList = listOf("Kanpur", "Lucknow")
        val adapter = ArrayAdapter(this@AddAddressActivity, R.layout.auto_list_item, cityList)
        binding.cityAutocompleteTextview.setAdapter(adapter)

        binding.cityAutocompleteTextview.setOnItemClickListener { _, _, position, _ ->
            city = adapter.getItem(position).toString()
        }*/

        binding.btnAddAddress.setOnClickListener {

            if (binding.tvFirstName.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter First Name")
            } else if (binding.tvLastName.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter Last Name")
            } else if (binding.tvMobile.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter Mobile Number")
            } else if (binding.tvMobile.editText?.text.toString().startsWith("0")) {
                Utility().showToast(this@AddAddressActivity, "Please enter a valid mobile number")
            } else if (binding.tvMobile.editText?.text.toString().length < 10) {
                Utility().showToast(this@AddAddressActivity, "Please enter a valid mobile number")
            } else if (binding.tvHouse.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter house number")
            } else if (binding.tvApart.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter apartment name")
            } else if (binding.tvStreet.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter street name")
            } else if (binding.tvAddress.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter area details")
            } else if (binding.tvCity.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please select city")
            } else if (binding.tvPin.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter pincode")
            } else if (binding.tvPin.editText?.text?.startsWith("0") == true) {
                Utility().showToast(this@AddAddressActivity, "Invalid pincode")
            } else if (binding.tvPin.editText?.text.toString().length < 6) {
                Utility().showToast(this@AddAddressActivity, "Invalid pincode")
            } else if (addressType.equals("", ignoreCase = true)) {
                Utility().showToast(this@AddAddressActivity, "Please select address type")
            } else {
                binding.btnAddAddress.isEnabled = false


                if(Utility.isAppOnLine(this@AddAddressActivity,object : OnInternetCheckListener {
                        override fun onInternetAvailable() {
                            addAddressRequest()
                        }
                    }))
                    addAddressRequest()
            }
        }
    }

    private fun addAddressRequest() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.firstName, binding.tvFirstName.editText?.text.toString())
        jsonObject.addProperty(Constant.LastName, binding.tvLastName.editText?.text.toString())
        jsonObject.addProperty(Constant.contactNumber, binding.tvMobile.editText?.text.toString())
        jsonObject.addProperty(Constant.houseNo, binding.tvHouse.editText?.text.toString())
        jsonObject.addProperty(Constant.ApartName, binding.tvApart.editText?.text.toString())
        jsonObject.addProperty(Constant.street, binding.tvStreet.editText?.text.toString())
        jsonObject.addProperty(Constant.landmark, binding.tvAddress.editText?.text.toString())
        jsonObject.addProperty(Constant.city, binding.tvCity.editText?.text.toString())
        jsonObject.addProperty(Constant.pincode, binding.tvPin.editText?.text.toString())
        jsonObject.addProperty(Constant.nickName, binding.tvSelectAddress.editText?.text.toString())
        jsonObject.addProperty(Constant.lat, "")
        jsonObject.addProperty(Constant.long, "")

        if (isUpdateAddress) {
            jsonObject.addProperty(Constant.addressId, addressId)
            viewModel.updateAddressRequest(jsonObject).observe(this) {
                binding.btnAddAddress.isEnabled = true
                it.let {
                    if (it.status) {
                        Utility.showToast(
                            this@AddAddressActivity,
                            "Address update successfully..."
                        )
                        finish()
                    } else {
                        Utility().showToast(this@AddAddressActivity, "Invalid Address !")
                    }
                }
            }
        } else {
            viewModel.addAddressRequest(jsonObject).observe(this) {
                binding.btnAddAddress.isEnabled = true
                it.let {
                    if (it.status) {
                        Utility().showToast(
                            this@AddAddressActivity,
                            "Address Added successfully..."
                        )
                        finish()
                    } else {
                        Utility().showToast(this@AddAddressActivity, "Invalid Address !")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left,
            R.anim.slide_out_right);
    }
}