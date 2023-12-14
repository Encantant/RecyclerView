package com.example.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerview.databinding.ActivityMainBinding
import com.example.recyclerview.model.User
import com.example.recyclerview.model.UsersListener
import com.example.recyclerview.model.UsersService
import com.google.android.material.snackbar.Snackbar
import java.util.Collections
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UsersAdapter


    private val usersService: UsersService
        get() = (applicationContext as App).usersService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var columnsCount = 2

        val touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(UP or DOWN or LEFT or END, LEFT);
                }

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    Collections.swap(usersService.getUsers(), viewHolder.layoutPosition, target.layoutPosition)
                    adapter.notifyItemMoved(viewHolder.layoutPosition, target.layoutPosition)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val pos = viewHolder.layoutPosition
                    when (direction){
                        LEFT -> {
                            val u = usersService.getUser(pos) //deleted item
                            val uID=usersService.getUsers().indexOf(u) //deleted item ID in list
                            usersService.deleteUserStrict(pos)
                            //usersService.deleteUser(usersService.getUser(pos))
                            adapter.notifyItemRemoved(pos)
                            Snackbar.make(binding.recyclerView,u.name,Snackbar.LENGTH_LONG)
                                .setAction("Undo", View.OnClickListener {
                                    usersService.addUser(uID, u)//u.id,u.name,u.company,u.photo,u.isLiked)
                                    adapter.notifyItemInserted(pos)
                                }).show()
                        }
                    }
                }

//                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
//                    RecyclerViewSwipeDecorator.Builder(this@MainActivity, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
//                        .addSwipeLeftActionIcon(R.drawable.ic_delete_black_24dp)
//                        .addSwipeRightBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
//                        .addSwipeRightActionIcon(R.drawable.ic_archive_black_24dp)
//                        .setActionIconTint(ContextCompat.getColor(recyclerView.context, android.R.color.white))
//                        .create()
//                        .decorate()
//                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return super.getSwipeThreshold(viewHolder)/columnsCount
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.height
                    val isCancelled = dX == 0f && !isCurrentlyActive
                    if (isCancelled) {
                        //clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), Paint())
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        return
                    }
                    val deleteDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete_black_24dp);
                    val intrinsicWidth = deleteDrawable!!.getIntrinsicWidth();
                    val intrinsicHeight = deleteDrawable.getIntrinsicHeight();
                    val mBackground = ColorDrawable()
                    mBackground.color = Color.parseColor("#D81B60")//"#b80f0a")
                    mBackground.setBounds(itemView.right + (dX*1/columnsCount).toInt(), itemView.top, itemView.right, itemView.bottom)
                    mBackground.draw(c)
                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val deleteIconMargin = (itemHeight - intrinsicHeight) / (columnsCount * columnsCount)
                    val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                    val deleteIconRight = itemView.right - deleteIconMargin
                    val deleteIconBottom = deleteIconTop + intrinsicHeight
                    deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    deleteDrawable.draw(c)
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

            }
        )

        touchHelper.attachToRecyclerView(binding.recyclerView)

        adapter = UsersAdapter(object : UserActionListener {
            override fun onUserMove(user: User, moveBy: Int) {
                usersService.moveUser(user, moveBy)
            }

            override fun onUserDelete(user: User) {
                usersService.deleteUser(user)
            }

            override fun onUserDetails(user: User) {
                Toast.makeText(this@MainActivity, "User: ${user.name}", Toast.LENGTH_SHORT).show()
            }

            override fun onPersonLike(user: User) {
                usersService.likeUser(user)
            }
        })

        val layoutManager = GridLayoutManager(this,columnsCount)
        //val imgLike = findViewById<ImageView>(R.id.likedImageView)
        //imgLike.isVisible= false

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        usersService.addListener(usersListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        usersService.removeListener(usersListener)
    }

    private val usersListener: UsersListener = {
        adapter.users = it
    }
}