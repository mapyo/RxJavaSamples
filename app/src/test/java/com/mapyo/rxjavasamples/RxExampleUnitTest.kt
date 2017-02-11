package com.mapyo.rxjavasamples

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
        val list = getStringMutableList(10)

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

    @Test @Throws(Exception::class)
    fun sample4() {
        val source = PublishSubject.create<String>()
        var running = true

        val list = getStringMutableList(3)

        Observable.create<String> {
            emitter ->
            while (running) {
                showMessage("ho1")
                Thread.sleep(300L)
                if (list.size > 0) {
                    source.onNext(list.removeAt(0))
                } else {
                    emitter.onComplete()
                }
            }
            emitter.onComplete()
        }
                .subscribeOn(Schedulers.computation())
                .subscribe(object : DisposableObserver<String>() {
                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }

                    override fun onNext(t: String?) {
                        showMessage(t)
                    }

                    override fun onComplete() {
                        showMessage("onComplete")
                    }

                })

        println("start?")
        Thread.sleep(3000L)
        running = false
        println("finish?")

    }

    @Test @Throws(Exception::class)
    fun sample5() {
        var observableNumber = 0
        val source = PublishSubject.create<String>()
        val maxCounter = 20

        Observable.create<String> {
            emitter ->
            observableNumber++
            for(counter in 1..maxCounter) {
                Thread.sleep(100L)
                showMessage("observableNumber" + observableNumber + "counter: " + counter)
                source.onNext(counter.toString())
            }
            emitter.onComplete()
        }
                .subscribeOn(Schedulers.computation())
                .subscribe(getObserver("main"))

        val sub1 = getObserver("sub1")
        val sub2 = getObserver("sub2")

        println("start?")
        source.subscribe(sub1)
        Thread.sleep(300L)
        source
                .filter {counter ->
                    counter != "5"
                }
                .subscribe(sub2)

        Thread.sleep(1000L)
        source.onComplete()
        println("finish?")
    }

    @Test @Throws(Exception::class)
    fun sample6() {
        val list = listOf("foo", "bar", "boo")
        Observable.fromIterable(list)
                .subscribe(::println)
    }

    private fun getStringMutableList(count: Int): MutableList<String> {
        val list = mutableListOf<String>()
        (1..count).mapTo(list) { "hoge" + it }
        return list
    }

    private fun getObserver(tag : String): DisposableObserver<String> {
        return object : DisposableObserver<String>() {
                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }

                    override fun onNext(t: String?) {
                        showMessage(tag + ":" + t)
                    }

                    override fun onComplete() {
                        showMessage(tag + ":" + "onComplete")
                    }

                }
    }

    private fun showMessage(message: String?) {
        val threadName = Thread.currentThread().name
        println(threadName + ":" + message)
    }
}
