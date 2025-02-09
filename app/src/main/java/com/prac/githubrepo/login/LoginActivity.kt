package com.prac.githubrepo.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.prac.githubrepo.BuildConfig
import com.prac.githubrepo.main.MainActivity
import com.prac.githubrepo.R
import com.prac.githubrepo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.prac.githubrepo.login.LoginViewModel.UiState
import com.prac.githubrepo.login.LoginViewModel.Event
import com.prac.githubrepo.login.LoginViewModel.SideEffect

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect {
                    handleEvent(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sideEffect.collect {
                    handleSideEffect(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    handleUiState(it)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            viewModel.setSideEffect(SideEffect.LoginButtonClick)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleUiState(uiState: UiState) {
        when (uiState) {
            is UiState.Idle -> { }
            is UiState.Loading -> {
                binding.includeProgressBar.root.isVisible = true
            }
            is UiState.Error -> {
                AlertDialog.Builder(this@LoginActivity)
                    .setMessage(uiState.errorMessage)
                    .setPositiveButton(R.string.check) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setOnDismissListener {
                        viewModel.setSideEffect(SideEffect.ErrorAlertDialogDismiss)
                    }
                    .show()
            }
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is Event.Success -> {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)

                finish()
            }
        }
    }

    private fun handleSideEffect(sideEffect: SideEffect) {
        when (sideEffect) {
            is SideEffect.LoginButtonClick -> {
                login()
            }
            is SideEffect.ErrorAlertDialogDismiss -> {
                viewModel.setUiState(UiState.Idle)
            }
        }
    }

    private fun login() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.GITHUB_OAUTH_URI))
        startActivity(intent)
    }

    private fun handleIntent(receiveIntent: Intent?) {
        receiveIntent?.let { intent ->
            if (intent.action == Intent.ACTION_VIEW) {
                intent.data?.let { uri ->
                    uri.getQueryParameter("code")?.let { code ->
                        viewModel.loginWithGitHub(code)
                    }
                }
            }
        }
    }
}