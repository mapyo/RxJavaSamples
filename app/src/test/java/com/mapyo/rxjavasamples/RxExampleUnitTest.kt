package com.mapyo.rxjavasamples

import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
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
            if (emitter.isDisposed) {
                return@create
            }

            emitter.onNext("Hello, World!")
            emitter.onNext("こんにちは、世界！")

            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }

        observableGreeting
                .observeOn(Schedulers.computation())
                .subscribe(object : DisposableObserver<String>(){
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

    @Test @Throws(Exception::class)
    fun sample3() {
        val list = mutableListOf<String>()
        (1..4).mapTo(list) { "hoge" + it }

        val unit = Observable.create<String> {
            emitter ->
            Thread.sleep(300L)
            val hoge = list.removeAt(0)
            println(hoge)
            emitter.onNext(hoge)
            emitter.onComplete()
        }

        unit.observeOn(Schedulers.computation())
                .repeat(10)
                .subscribe(object : DisposableObserver<String>() {
                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        println("error!!!")
                    }

                    override fun onNext(t: String?) {
                        val threadName = Thread.currentThread().name
                        println(threadName + ":" + t)
                    }

                    override fun onComplete() {
                        val threadName = Thread.currentThread().name
                        println(threadName + "完了しました")
                    }
                })
        Thread.sleep(1000L)
    }
}
