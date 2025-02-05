package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    fun generateCatFact(): Single<Fact> {
        return Single.fromCallable {
            Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        }
    }

    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .map {
                Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
            }
            .distinctUntilChanged()
    }
}