package pl.ownvision.scorekeeper.views.activities

import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.PopupMenu
import com.elvishew.xlog.XLog
import com.github.nitrico.lastadapter.BR
import com.github.nitrico.lastadapter.LastAdapter
import kotlinx.android.synthetic.main.activity_main.*
import pl.ownvision.scorekeeper.R
import pl.ownvision.scorekeeper.core.*
import pl.ownvision.scorekeeper.databinding.ItemGameLayoutBinding
import pl.ownvision.scorekeeper.exceptions.ValidationException
import pl.ownvision.scorekeeper.models.Game
import pl.ownvision.scorekeeper.repositories.GameRepository
import javax.inject.Inject


class MainActivity : BaseActivity() {

    val games = ObservableArrayList<Game>()
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
                        GameActivityStarter.start(application, game.id)
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

        games.addAll(gameRepository.getAllGames())
    }

    fun createNewGame(){
        showInputDialog(R.string.new_game, R.string.create, getString(R.string.name_placeholder), null) {input ->
            try {
                val game = gameRepository.createGame(input.toString())
                games.add(0, game)
            }catch (e: ValidationException){
                this.snackbar(e.error)
            }catch (e: Exception){
                this.snackbar(R.string.error_creating_game)
                XLog.e("Error creating game", e)
            }
        }
    }

    fun displayPopup(view: View, game: Game){
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_game_item)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_game_remove -> {
                    removeGame(game)
                    true
                }
                R.id.menu_game_rename -> {
                    renameGame(game)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun removeGame(game: Game){
        // TODO : confirm dialog
        try {
            gameRepository.removeGame(game)
            games.remove(game)
        }catch (e: Exception){
            this.snackbar(R.string.error_deleting_game)
            XLog.e("Error removing game", e)
        }
    }

    fun renameGame(game: Game){
        showInputDialog(R.string.rename, R.string.save, getString(R.string.name_placeholder), game.name) {input ->
            try {
                game.name = input.toString()
                gameRepository.updateGame(game)
                lastAdapter.notifyDataSetChanged()
            }catch (e: ValidationException){
                this.snackbar(e.error)
            }catch (e: Exception){
                this.snackbar(R.string.error_renaming_game)
                XLog.e("Error rename game", e)
            }
        }
    }
}
