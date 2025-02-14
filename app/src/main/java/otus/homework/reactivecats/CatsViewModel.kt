package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        catsService
        disposable.clear()
    }

    fun getFacts() {
        disposable.add(
            Observable.interval(2000, TimeUnit.MILLISECONDS)
                .flatMapSingle {
                    catsService.getCatFact()
                        .subscribeOn(Schedulers.io())
                        .map<Result> { fact -> Success(fact) }
                        .onErrorResumeNext {
                            localCatFactsGenerator.generateCatFact()
                                .map<Result> { fact -> Success(fact) }
                        }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        _catsLiveData.value = result
                    },
                    { error ->
                        _catsLiveData.value = Error(
                            error.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                )
        )
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()