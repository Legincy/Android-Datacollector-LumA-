package pl.peth.datacollector.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.peth.datacollector.ui.bottomNav.PositionFragmentViewModel
import pl.peth.datacollector.ui.bottomNav.SensorFragmentViewModel

val viewModel = module {
    viewModel { SensorFragmentViewModel() }
    viewModel {PositionFragmentViewModel(context = get())}
}