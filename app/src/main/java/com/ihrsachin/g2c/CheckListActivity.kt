package com.ihrsachin.g2c

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import android.widget.LinearLayout


class CheckListActivity : AppCompatActivity() {

    private lateinit var mFirebaseDatabase : FirebaseDatabase
    private lateinit var listDatabaseReference: DatabaseReference
    private lateinit var mChildEventListener: ChildEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_list)

        //initializing database
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        //mFirebaseDatabase.setPersistenceEnabled(true)
        listDatabaseReference = mFirebaseDatabase.reference.child("check_list")

        val check_list : ArrayList<String> = ArrayList()
        val listView : ListView = findViewById(R.id.listView)
        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, check_list)
        listView.adapter = mAdapter


        val float_btn : FloatingActionButton = findViewById(R.id.add_btn)


        float_btn.setOnClickListener{

            // AlertDialog
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            //my custom view for alert builder
            val mDialogView : View = layoutInflater.inflate(R.layout.add_item_dialog, null)
            val mInputView: LinearLayout = mDialogView.findViewById(R.id.add_item_dialog)

            //adding my custom view to the alert dialog
            builder.setView(mInputView)
            val alert : AlertDialog = builder.create()  // alert to show and close the dialog

            mInputView[1].setOnClickListener{
                val input : EditText = mInputView[0] as EditText
                val item = input.text.toString()

                //pushing item to the firebase database
                if(mAdapter.getPosition(item) >= 0) input.error = getString(R.string.error_already_exist)
                else if(item.isNotEmpty() && item.isNotBlank()) {
                    listDatabaseReference.push().setValue(item)
                    alert.cancel()
                }
                else input.error = getString(R.string.error_for_empty_list)
            }
            alert.show()
        }


        listView.setOnItemLongClickListener{ parent, view, position, id ->
            val textview = TextView(this)
            textview.textSize = 20f
            textview.text = getString(R.string.delete_warning)
            textview.gravity = Gravity.CENTER


            //my custom view for alert builder
            val mDialogView : View = layoutInflater.inflate(R.layout.add_item_dialog, null)
            val mInputView: LinearLayout = mDialogView.findViewById(R.id.add_item_dialog)

            mInputView.removeViewAt(0)
            mInputView.addView(textview, 0)

            (mInputView[1] as Button).text = getString(R.string.delete)

            // AlertDialog
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            //adding my custom view to the alert dialog
            builder.setView(mInputView)
            val alert : AlertDialog = builder.create()  // alert to show and close the dialog

            mInputView[1].setOnClickListener{
                listDatabaseReference.orderByValue().equalTo(mAdapter.getItem(position).toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            snapshot.ref.removeValue()  //removing target item
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                alert.cancel()
            }
            alert.show()
            true
        }




        // onEventChangedListener
        mChildEventListener  = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val item : String = (snapshot.value as String?)!!
                mAdapter.add(item)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val item : String = (snapshot.value as String?)!!
                mAdapter.remove(item)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        listDatabaseReference.addChildEventListener(mChildEventListener)

    }
}