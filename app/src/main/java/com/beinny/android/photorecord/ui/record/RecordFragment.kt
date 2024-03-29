package com.beinny.android.photorecord.ui.record

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.beinny.android.photorecord.*
import com.beinny.android.photorecord.model.Record
import com.beinny.android.photorecord.ui.common.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*
import com.beinny.android.photorecord.databinding.*
import com.beinny.android.photorecord.ui.common.OrderKoreanFirst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordFragment : Fragment() {
    /** [Host Activity 에서 구현할 callback 함수의 인터페이스] */
    interface Callbacks {
        fun onSelected(id:UUID)
        fun onLongClick(longclick:Boolean,count:Int)
    }

    /** [액티비티의 콜백 저장] */
    private var callbacks: Callbacks? =null

    /** [back press 처리 콜백] */
    private lateinit var callbacksBp: OnBackPressedCallback
    private var backKeyPressedTime : Long = 0

    private val viewModel: RecordViewModel by viewModels { ViewModelFactory(requireContext()) }
    private lateinit var binding: FragmentRecordBinding

    private var longClick: Boolean = false
    private var countOfCheckedRecord : Int = 0

    private lateinit var recordAdapter : RecordAdapter

    override fun onAttach(context: Context) { // fragment가 add 될때 호출
        super.onAttach(context)
        /** [액티비티의 콜백 저장] */
        callbacks = context as Callbacks?

        /** [back press 처리 콜백] */
        callbacksBp = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /** [longClick 상태일 경우] */
                if (longClick) {
                    disableLongClick()
                    // UI 갱신
                    val record = Record()
                    viewModel.addRecord(record)
                    viewModel.deleteRecord(record)
                }
                /** [백 버튼 두번] */
                else if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis()
                    Toast.makeText(context,getString(R.string.record_app_close_warning), Toast.LENGTH_SHORT).show()
                }
                else {
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callbacksBp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** [RecordListFragment가 메뉴 콜백(onCreateOptionsMenu) 호출을 받아야함을 Fragment Manager 에게 알려준다] */
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** [뷰 바인딩] */
        binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** [recyclerView 레이아웃 설정] */
        binding.rvRecordList.layoutManager = GridLayoutManager(context,2)

        recordAdapter = RecordAdapter(callbacks,AdapterCallback())
        binding.rvRecordList.adapter = recordAdapter

        /** [뷰 모델의 LiveData observe] */
        viewModel.recordListLiveData.observe(
            viewLifecycleOwner,
            Observer { records ->
                records?.let {
                    CoroutineScope(Dispatchers.Main).launch{
                        val sortedRecords = withContext(Dispatchers.Default){
                            sortRecords(PhotoRecordApplication.prefs.getInt("SORT_BY",3),records)
                        }
                        recordAdapter.submitList(sortedRecords) // 데이터 추가/변경시 ListAdapter에게 submitList()를 통해 알려준다.
                    }
                }
            })
    }

    override fun onStart() {
        super.onStart()
        /** [fab 리스너 - Record 생성] */
        binding.fabRecordAdd.setOnClickListener {
            // 새로운 Record 객체 생성
            val record = Record()
            // DB에 추가.
            viewModel.addRecord(record)
            // 액티비티에 구현된 onSelected 콜백함수를 호출. 새로 추가된 레코드의 상세화면이 화면에 보이도록.
            callbacks?.onSelected(record.id)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
        callbacksBp.remove()
    }

    /** [메뉴 인플레이트] */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_record_sort_or_delete, menu)

        val sortMenu = menu.findItem(R.id.sort_record)
        val deleteMenu = menu.findItem(R.id.delete_record)

        if (longClick) {
            if(countOfCheckedRecord == 0) {
                sortMenu.isVisible = false
                deleteMenu.isVisible = false
            } else {
                sortMenu.isVisible = false
                deleteMenu.isVisible = true
            }
        }
        else {
            sortMenu.isVisible = true
            deleteMenu.isVisible = false
        }
    }

    /** [메뉴 선택] */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_record -> {
                showSortDialog()
                true
            }
            R.id.delete_record -> {
                deleteCheckedRecords()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /** [정렬을 위한 Dialog 출력] */
    private fun showSortDialog() {
        val dlg = BottomSheetDialog(requireContext(),R.style.transparentDialog)
        val dlgBinding = DialogSortingBinding.inflate(LayoutInflater.from(requireContext()))
        dlg.setContentView(dlgBinding.root)

        when (PhotoRecordApplication.prefs.getInt("SORT_BY",3)) {
            0 -> dlgBinding.rbDialogSortingSortbyNameAsc.isChecked = true
            1 -> dlgBinding.rbDialogSortingSortbyNameDesc.isChecked = true
            2 -> dlgBinding.rbDialogSortingSortbyDateAsc.isChecked = true
            3 -> dlgBinding.rbDialogSortingSortbyDateDesc.isChecked = true
        }

        var selected = 3
        dlgBinding.radioGroup.setOnCheckedChangeListener { _, checkedId:Int ->
            selected = when(checkedId) {
                R.id.rb_dialog_sorting_sortby_name_asc -> 0
                R.id.rb_dialog_sorting_sortby_name_desc -> 1
                R.id.rb_dialog_sorting_sortby_date_asc -> 2
                R.id.rb_dialog_sorting_sortby_date_desc -> 3
                else -> -1
            }
            Log.d("sort",selected.toString())
        }

        dlgBinding.tvDialogSortingComplete.setOnClickListener {
            PhotoRecordApplication.prefs.setInt("SORT_BY",selected)
            //TODO: 화면 갱신 필요. 옵저버 작동 OR 여기서 갱신.
            val record = Record()
            viewModel.addRecord(record)
            viewModel.deleteRecord(record)
            dlg.dismiss()
            Log.d("sort",PhotoRecordApplication.prefs.getInt("SORT_BY",3).toString())
        }

        dlgBinding.tvDialogSortingCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()

    }

    /** [Record 정렬] */
    private fun sortRecords(sortBy:Int, records: List<Record>): List<Record> {
        val korEngNumSpec = Comparator<Record> { data1, data2 -> OrderKoreanFirst.compare(data1.label, data2.label) }
        if(sortBy == 0){ // 이름(오름차순)
            return records.sortedWith(korEngNumSpec)
        }
        else if(sortBy == 1){ // 이름(내림차순)
            return records.sortedWith(korEngNumSpec).reversed()
        }
        else if(sortBy == 2){ // 날짜(오름차순)
            return records.sortedWith(compareBy<Record> { it.date }.thenComparator{ data1, data2 -> OrderKoreanFirst.compare(data1.label, data2.label) })
        }
        else if(sortBy == 3){ // 날짜(내림차순)
            return records.sortedWith(compareByDescending<Record> { it.date }.thenComparator{ data1, data2 -> OrderKoreanFirst.compare(data1.label, data2.label) })
        }
        else{
            return records
        }
    }

    private fun disableLongClick() {
        longClick = false
        binding.fabRecordAdd.visibility = View.VISIBLE
        requireActivity().invalidateOptionsMenu()
        callbacks?.onLongClick(longClick,countOfCheckedRecord)
    }

    /** [체크 된 Record 모두 삭제] */
    private fun deleteCheckedRecords() {
        val dlg = BottomSheetDialog(requireContext(), R.style.transparentDialog)
        val dlgBinding = DialogAlertBinding.inflate(LayoutInflater.from(requireContext()))
        dlg.setContentView(dlgBinding.root)

        dlgBinding.tvDialogAlertMsg.text =
            getString(R.string.record_selected_delete_warning_1) + countOfCheckedRecord.toString() + getString(R.string.record_selected_delete_warning_2)

        dlgBinding.tvDialogAlertComplete.setOnClickListener {
            disableLongClick()
            viewModel.deleteCheckedRecord()
            dlg.dismiss()
        }
        dlgBinding.tvDialogAlertCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    /** [RecordAdapter로 넘겨줄 callback] */
    inner class AdapterCallback {
        fun activateLongClick(record:Record) {
            longClick = true

            viewModel.initCheck(record.id,true)
            countOfCheckedRecord = 1

            requireActivity().invalidateOptionsMenu()
            binding.fabRecordAdd.visibility = View.INVISIBLE
            callbacks?.onLongClick(longClick,countOfCheckedRecord)
        }
        fun isLongClick() : Boolean{
            return longClick
        }
        fun changeCheck(id:UUID,state:Boolean) {
            if (state) {
                countOfCheckedRecord += 1
            } else { countOfCheckedRecord -= 1 }
            viewModel.changeCheck(id,state)
            callbacks?.onLongClick(longClick,countOfCheckedRecord)
            if (countOfCheckedRecord == 0) {
                requireActivity().invalidateOptionsMenu()
            } else if ((countOfCheckedRecord == 1) and state){
                requireActivity().invalidateOptionsMenu()
            }
        }
    }

    companion object {
        fun newInstance(): RecordFragment {
            return RecordFragment()
        }
    }

    /*
    private lateinit var recordRecyclerView: RecyclerView
    private var adapter : RecordAdapter? = RecordAdapter(emptyList())

    private val recordListViewModel: RecordViewModel by lazy {
        ViewModelProvider(this).get(RecordViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record, container, false)

        recordRecyclerView = view.findViewById(R.id.rv_record_list) as RecyclerView
        recordRecyclerView.layoutManager = GridLayoutManager(context,2)

        recordRecyclerView.adapter = adapter

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_record -> {
                // 새로운 Record 객체 생성
                val record = Record()
                // DB에 추가.
                viewModel.addRecord(record)
                // 액티비티에 구현된 onSelected 콜백함수를 호출. 새로 추가된 레코드의 상세화면이 화면에 보이도록.
                callbacks?.onSelected(record.id)
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(records: List<Record>) {
        adapter = RecordAdapter(records)
        recordRecyclerView.adapter = adapter

        // 데이터 추가/변경시 ListAdapter에게 submitList()를 통해 알려준다.
        adapter?.submitList(records)
    }

    /** [UI 갱신 (recyclerView)] */
    private fun updateUI(records: List<Record>) {
        /** [UI 반영 이전, Record 정렬 (SharedPreference)] */
        val sortedRecords = sortRecords(PhotoRecordApplication.prefs.getInt("SORT_BY",-1),records)

        val recordAdapter = RecordAdapter(callbacks,adapterCallback())
        binding.rvRecordList.adapter = recordAdapter
        recordAdapter.submitList(sortedRecords) // 데이터 추가/변경시 ListAdapter에게 submitList()를 통해 알려준다.
    }

    private inner class RecordHolder(view:View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var record: Record

        val labelTextView: TextView = itemView.findViewById(R.id.tv_label_thumbnail)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_date_thumbnail)
        val photoView: ImageView = itemView.findViewById(R.id.tv_photo_thumbnail)

        init{
            itemView.setOnClickListener(this)
        }

        fun bind(record:Record){
            this.record = record
            labelTextView.text = this.record.label
            val df = SimpleDateFormat("yyyy/M/d", Locale.KOREA)
            dateTextView.text = df.format(this.record.date)

            var thumbFile = viewModel.getThumbFile(record) // 썸네일 이미지 파일의 위치를 가르키는 속성
            var photoFile = viewModel.getPhotoFile(record) // 원본 이미지 파일의 위치를 가르키는 속성

            /** [photoView (thumbnail) 업데이트] */
            // 1. 썸네일 파일이 있을 경우
            if (thumbFile.exists()){
                val bitmap = getScaledBitmap(thumbFile.path, requireActivity(), GET_BIMAP_ORIGIN)
                photoView.setImageBitmap(bitmap)
            }
            else {
                // 2. 썸네일 없음 + 원본 이미지 파일은 존재할 경우 썸네일 생성
                if (photoFile.exists()){
                    val thumb_file = File(thumbFile.path)
                    try {
                        val bitmap_thumb = getScaledBitmap(photoFile.path, requireActivity(), GET_BIMAP_RESIZE)
                        val out = FileOutputStream(thumb_file)
                        bitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.close()
                        val bitmap = getScaledBitmap(thumbFile.path, requireActivity(), GET_BIMAP_ORIGIN)
                        photoView.setImageBitmap(bitmap)
                    } catch(e:Exception) {
                        Toast.makeText(context,"error : 내부 저장소에 이미지를 저장 할 수 없음",Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
                // 3. 이미지를 등록하지 않은 레코드일 경우
                else{
                    photoView.setImageDrawable(null)
                }
            }
        }

        override fun onClick(v:View){
            callbacks?.onSelected(record.id)
        }
    }

    private inner class RecordAdapter(var records: List<Record>) : ListAdapter<Record, RecordHolder>(RecordDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordHolder {
            val view = layoutInflater.inflate(R.layout.item_record,parent,false)
            return RecordHolder(view)
        }

        override fun onBindViewHolder(holder: RecordHolder, position: Int) {
            val record = records[position]
            holder.bind(record)
        }
    }

    private class RecordDiffCallback : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            // return oldItem == newItem
            return oldItem.equals(newItem)
        }
    }
    */
}