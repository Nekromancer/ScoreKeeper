package pl.ownvision.scorekeeper.views.activities

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.PopupMenu
import com.afollestad.materialdialogs.MaterialDialog
import com.elvishew.xlog.XLog
import com.github.nitrico.lastadapter.BR
import com.github.nitrico.lastadapter.LastAdapter
import kotlinx.android.synthetic.main.activity_main.*
import pl.ownvision.scorekeeper.R
import pl.ownvision.scorekeeper.core.*
import pl.ownvision.scorekeeper.databinding.ItemGameLayoutBinding
import pl.ownvision.scorekeeper.db.entities.Game
import pl.ownvision.scorekeeper.exceptions.ValidationException


class MainActivity : BaseActivity() {

    val games = mutableListOf<Game>()
    lateinit var lastAdapter: LastAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar(toolbar_include)

        fab.setOnClickListener {
            createNewGame()
        }

        games_list.setHasFixedSize(true)
        games_list.layoutManager = LinearLayoutManager(this)

        lastAdapter = LastAdapter(games, BR.game, true)
                .map<Game, ItemGameLayoutBinding>(R.layout.item_game_layout) {
                    onClick {
                        val game = it.binding.game ?: return@onClick
                        GameActivityStarter.start(this@MainActivity, game.id)
                    }

                    onBind {
                        val game = it.binding.game ?: return@onBind
                        val view = it.itemView.findViewById(R.id.textViewOptions)
                        view.setOnClickListener {
                            displayPopup(it, game)
                        }
                    }
                }
                .into(games_list)

        database.gameDao().getAll().observe(this, Observer {
            if(it != null) games.addAll(it)
        })

    }

    fun createNewGame(){
        showInputDialog(R.string.new_game, R.string.create, getString(R.string.name_placeholder), null) {input ->
            try {
                if(input.isNullOrEmpty()) throw ValidationException(getString(R.string.validation_name_cannot_be_empty))
                val game = Game(name = input)
                database.gameDao().insert(game)
                games.add(0, game)
            }catch (e: ValidationException){
                this.snackbar(e.error)
            }catch (e: Exception){
                this.snackbar(R.string.error_creating)
                XLog.e("Error creating game", e)
            }
        }
    }

    fun displayPopup(view: View, game: Game){
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_standard_item)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_standard_remove -> {
                    removeGame(game)
                    true
                }
                R.id.menu_standard_rename -> {
                    renameGame(game)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun removeGame(game: Game){
        MaterialDialog.Builder(this)
                .title(R.string.remove_confirm)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive { _, _ ->
                    try {
                        database.gameDao().delete(game)
                        games.remove(game)
                    }catch (e: Exception){
                        this.snackbar(R.string.error_deleting)
                        XLog.e("Error removing game", e)
                    }
                }
                .show()
    }

    fun renameGame(game: Game){
        showInputDialog(R.string.rename, R.string.save, getString(R.string.name_placeholder), game.name) {input ->
            try {
                if(input.isNullOrEmpty()) throw ValidationException(getString(R.string.validation_name_cannot_be_empty))
                database.gameDao().setName(game.id, input)
                val updatedGame = database.gameDao().get(game.id)
                val index = games.indexOfFirst { it.id == game.id }
                games[index] = updatedGame
                lastAdapter.notifyDataSetChanged()
            }catch (e: ValidationException){
                this.snackbar(e.error)
            }catch (e: Exception){
                this.snackbar(R.string.error_renaming)
                XLog.e("Error rename game", e)
            }
        }
    }
}
