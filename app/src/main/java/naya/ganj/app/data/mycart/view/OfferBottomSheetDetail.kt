package naya.ganj.app.data.mycart.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import naya.ganj.app.R
import naya.ganj.app.data.mycart.adapter.OfferDetailAdapter
import naya.ganj.app.data.mycart.model.CouponModel

class OfferBottomSheetDetail(val promoCode: CouponModel.PromoCode) : BottomSheetDialogFragment() {

    var recyclerView: RecyclerView? = null
    private var tvName: TextView?=null
    var tvOffer: TextView?=null
    var tvDesription: TextView?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.coupon_item_layout, container, false)

        recyclerView = view.findViewById(R.id.rv_coupon_detail_list)
        tvName=view.findViewById(R.id.tv_name)
        tvOffer=view.findViewById(R.id.tv_offer)
        tvDesription=view.findViewById(R.id.tv_description)

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName?.text=promoCode.name
        tvOffer?.text=promoCode.offer
        tvDesription?.text=promoCode.description
        recyclerView?.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView?.adapter=OfferDetailAdapter(promoCode.details)
        recyclerView?.visibility = View.VISIBLE


    }

}