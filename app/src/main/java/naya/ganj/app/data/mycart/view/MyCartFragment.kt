package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.R
import naya.ganj.app.data.mycart.adapter.MyCartAdapter
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.viewmodel.MyCartViewModel
import naya.ganj.app.databinding.FragmentMycartLayoutBinding
import naya.ganj.app.interfaces.OnSavedAmountListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.utility.Constant.ORDER_ID
import naya.ganj.app.utility.Utility
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyCartFragment : Fragment(), OnclickAddOremoveItemListener, OnSavedAmountListener {

    lateinit var myCartViewModel: MyCartViewModel
    lateinit var binding: FragmentMycartLayoutBinding
    lateinit var myCartModel: MyCartModel
    lateinit var addressId: String
    var walletBalance = ""
    private val arguments: MyCartFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myCartViewModel = ViewModelProvider(requireActivity()).get(MyCartViewModel::class.java)
        binding = FragmentMycartLayoutBinding.inflate(inflater, container, false)

        binding.btnChangeAddress.setOnClickListener {
            val intent = Intent(requireActivity(), AddressListActivity::class.java)
            intent.putExtra("ADDRESS_ID", addressId)
            startActivity(intent)
        }

        binding.btnCheckoutButton.setOnClickListener {
            val intent = Intent(requireActivity(), PaymentOptionActivity::class.java)
            intent.putExtra("TOTAL_AMOUNT", binding.tvFinalAmount.text.toString())
            intent.putExtra("ADDRESS_ID", addressId)
            intent.putExtra("PROMO_CODE", "")
            intent.putExtra("WALLET_BALANCE", walletBalance)
            startActivity(intent)

        }
        binding.btnLoginButton.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

        binding.btnShopNow.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                getMyCartData(arguments.orderId)
            }
        }
    }


    private fun getMyCartData(orderId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.mainConstraintLayout.visibility = View.GONE
        val jsonObject = JsonObject()
        jsonObject.addProperty(ORDER_ID, orderId)
        myCartViewModel.getMyCartData(jsonObject).observe(requireActivity()) {
            if (isAdded) {
                myCartModel = it
                setListData(it)
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    if (isAdded)
                        calculateAmount()
                    // setSavedAmount()
                }, 300)

                // Set Saved Amount
                /* for (item in it.cartList) {
                     lifecycleScope.launch(Dispatchers.IO) {
                         val savedAmountModel =
                             SavedAmountModel(
                                 item.productId,
                                 item.variantId.toInt(),
                                 item.discountPrice.toDouble()
                             )

                         val isItemExist = AppDataBase.getInstance(requireActivity()).productDao()
                             .isSavedItemIsExist(item.productId, item.variantId.toInt())

                         if (isItemExist) {
                             AppDataBase.getInstance(requireActivity()).productDao()
                                 .updateAmount(
                                     item.discountPrice.toDouble(),
                                     item.productId,
                                     item.variantId.toInt()
                                 )
                         } else {
                             AppDataBase.getInstance(requireActivity()).productDao()
                                 .insertSavedAmount(savedAmountModel)
                         }
                     }
                 }*/
            }
        }
    }

    private fun setListData(mModel: MyCartModel?) {

        val myCartAdapter =
            mModel?.let { MyCartAdapter(requireActivity(), it.cartList, this, this) }
        binding.rvMycartList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvMycartList.adapter = myCartAdapter
        binding.nestedscrollview.isNestedScrollingEnabled = false
        binding.progressBar.visibility = View.GONE
        binding.mainConstraintLayout.visibility = View.VISIBLE

        setAddressDetail(myCartModel.address.address)

    }

    private fun setAddressDetail(address: MyCartModel.Address.Address) {
        addressId = myCartModel.address.id
        val addressString =
            address.houseNo + "," + address.apartName + "," + address.street + "," + address.landmark + "," +
                    address.city + "-" + address.pincode
        binding.tvAddressDetail.text = addressString

    }


    override fun onClickAddOrRemoveItem(
        action: String,
        productId: String,
        variantId: String,
        promoCode: String,
        totalAmount: Double
    ) {
        Utility().addRemoveItem(action, productId, variantId, promoCode)
        if (totalAmount > 0) {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                val isProductExist = async {
                    Utility().isProductAvailable(
                        requireActivity(),
                        productId,
                        variantId
                    )
                }.await()

                if (isProductExist) {
                    Utility().updateProduct(requireActivity(), productId, variantId, totalAmount)

                } else {
                    Utility().insertProduct(requireActivity(), productId, variantId, totalAmount)
                }
            }
        } else {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                Utility().deleteProduct(requireActivity(), productId, variantId)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 500)
    }

    private fun calculateAmount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listOfProduct = Utility().getAllProductList(requireActivity())
            Log.e("TAG", "calculateAmount: " + listOfProduct)
            requireActivity().runOnUiThread {
                if (listOfProduct.isNotEmpty()) {
                    binding.finalCheckoutLayout.visibility = View.VISIBLE
                    binding.constraintCartAmountLayout.visibility = View.VISIBLE
                    binding.constraintOfferLayout.visibility = View.VISIBLE
                    binding.materialAddressCardview.visibility = View.VISIBLE
                    binding.rvMycartList.visibility = View.VISIBLE
                    binding.emptyCartLayout.visibility = View.GONE

                    var cartAmount = 0.0
                    val totalAmount: Double
                    for (item in listOfProduct) {
                        cartAmount += item.totalAmount
                    }

                    cartAmount = Utility().formatTotalAmount(cartAmount)
                    if (cartAmount > myCartModel.deliveryChargesThreshHold) {
                        binding.tvDeliveryCharges.text = "0.0"
                        totalAmount = cartAmount
                    } else {
                        totalAmount = cartAmount + myCartModel.deliveryCharges
                        binding.tvDeliveryCharges.text = myCartModel.deliveryCharges.toString()
                    }
                    binding.tvCartAmount.text = cartAmount.toString()
                    binding.tvTotalAmount.text = totalAmount.toString()
                    binding.tvFinalAmount.text = Utility().formatTotalAmount(totalAmount).toString()
                } else {
                    binding.finalCheckoutLayout.visibility = View.GONE
                    binding.constraintCartAmountLayout.visibility = View.GONE
                    binding.constraintOfferLayout.visibility = View.GONE
                    binding.materialAddressCardview.visibility = View.GONE
                    binding.rvMycartList.visibility = View.GONE
                    binding.emptyCartLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSavedAmount(productId: String, variantId: Int, amount: Double) {

        if (amount > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val isItemExist = AppDataBase.getInstance(requireActivity()).productDao()
                    .isSavedItemIsExist(productId, variantId)
                if (isItemExist) {
                    AppDataBase.getInstance(requireActivity()).productDao()
                        .updateAmount(amount, productId, variantId)
                }
            }

        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDataBase.INSTANCE?.productDao()
                    ?.deleteAmount(productId, variantId)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded)
                setSavedAmount()
        }, 300)
    }


    private fun setSavedAmount() {
        var finalSaveAmount = 0.0
        lifecycleScope.launch(Dispatchers.IO) {
            val listOfSavedAmount =
                AppDataBase.getInstance(requireActivity()).productDao().getSavedAmountList()
            if (!listOfSavedAmount.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    if (listOfSavedAmount.isNotEmpty())
                        for (item in listOfSavedAmount) {
                            finalSaveAmount += item.totalAmount
                        }
                    binding.tvFinalSavedAmount.text =
                        Utility().formatTotalAmount(finalSaveAmount).toString()
                }
            }
        }
    }

}