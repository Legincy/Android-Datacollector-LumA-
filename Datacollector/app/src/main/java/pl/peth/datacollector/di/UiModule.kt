package pl.peth.datacollector.di

import android.app.Application
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.peth.datacollector.ui.bottomNav.SensorFragmentViewModel

val viewModel = module {
    viewModel { SensorFragmentViewModel(application = get()) }
}