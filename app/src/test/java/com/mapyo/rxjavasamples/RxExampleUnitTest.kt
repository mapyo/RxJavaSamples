package com.mapyo.rxjavasamples

import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import org.junit.Test



/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class RxExampleUnitTest {
    @Test @Throws(Exception::class)
    fun sample1() {
        Observable.just("Cricket", "Football")
                .subscribe(object: DisposableObserver<String>(){
                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }

                    override fun onComplete() {
                        println("onComplete")
                    }

                    override fun onNext(t: String?) {
                        println(t)
                    }
                })
    }

    @Test @Throws(Exception::class)
    fun sample2() {
        val observableGreeting : Observable<String> = Observable.create {
            emitter ->
            emitter.onNext("Hello, World!")
            emitter.onNext("こんにちは、世界！")
        }

        observableGreeting.subscribe(object : DisposableObserver<String>(){
            override fun onError(e: Throwable?) {
                e?.printStackTrace()
            }

            override fun onComplete() {
                val threadName = Thread.currentThread().name
                println(threadName + "完了しました")
            }

            override fun onNext(t: String?) {
                val threadName = Thread.currentThread().name
                println(threadName + ":" + t)
            }
        })
    }
}
