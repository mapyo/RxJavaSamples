package com.mapyo.rxjavasamples

import io.reactivex.*
import io.reactivex.functions.BiFunction
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class RxExampleUnitTest {
    @Test @Throws(Exception::class)
    fun sample1() {
        Observable.just("Cricket", "Football")
                .subscribe(object : DisposableObserver<String>() {
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
        val observableGreeting: Observable<String> = Observable.create {
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
                .subscribe(object : DisposableObserver<String>() {
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
            for (counter in 1..maxCounter) {
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
                .filter { counter ->
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

    @Test @Throws(Exception::class)
    fun sample7() {
        val list = getStringMutableList(10)
        val looper = Observable.create<String> {
            source ->

            try {
                Thread.sleep(100L)
            } catch (e: Exception) {
                source.onError(e)
            }

            if (list.size > 0) {
                val message = list.removeAt(0)
                showMessage(message)
                source.onNext(message)
            } else {
                source.onError(throw RuntimeException("no....."))
            }
            source.onComplete()
        }.subscribeOn(Schedulers.computation())
                .repeat(10).subscribe({
            showMessage("onNext")
        }, {
            showMessage("onError")
        })

        showMessage("start")
        Thread.sleep(500L)
        showMessage("go finish")
        looper.dispose()
        showMessage("finished!!!")
    }

    @Test @Throws(Exception::class)
    fun sample8_flowable() {

        val flowable = Flowable.create<String>({
            emitter: FlowableEmitter<String> ->
            for (i in 1..10) {
                if (emitter.isCancelled) {
                    return@create
                }

                emitter.onNext(i.toString())
            }

            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)

        flowable.observeOn(Schedulers.computation())
                .subscribe(object : Subscriber<String> {
                    private lateinit var subscription: Subscription

                    override fun onSubscribe(subscription: Subscription) {
                        this.subscription = subscription
                        this.subscription.request(1)
                    }

                    override fun onNext(message: String) {
                        showMessage(message)
                        subscription.request(1)
                    }

                    override fun onComplete() {
                        showMessage("完了しました")
                    }

                    override fun onError(t: Throwable?) {
                    }

                })

        Thread.sleep(500)
    }

    @Test @Throws(Exception::class)
    fun sample8() {
        val publisher = PublishSubject.create<String>()

        val flowable = Flowable.fromIterable<String>(getStringMutableList(3))

        // 送信
        flowable.observeOn(Schedulers.computation())
                .subscribe(object : Subscriber<String> {
                    private lateinit var subscription: Subscription

                    override fun onSubscribe(subscription: Subscription) {
                        this.subscription = subscription
                        this.subscription.request(1)
                    }

                    override fun onNext(message: String) {
                        showMessage(message)
                        val sendObservable = Observable.fromArray(message)
                                .doOnNext {
                                    showMessage("doOnNext")
                                }

                        // 待ち受ける
                        Observable.zip(sendObservable, publisher,
                                BiFunction<String, String, String> {
                                    send, receive ->
                                    val message = send + ":" + receive
                                    showMessage(message)
                                    subscription.request(1)
                                    message
                                }).subscribe()

                        // 送信する
                        publisher.onNext("receive: " + System.currentTimeMillis())
                    }

                    override fun onComplete() {
                        showMessage("完了しました")
                    }

                    override fun onError(t: Throwable?) {
                        t?.printStackTrace()
                    }

                })

        Thread.sleep(1000)
    }

    @Test @Throws(Exception::class)
    fun sample9() {
        Flowable.just("a", "b", "c")
                .flatMap {
                    Flowable.just(it)
                            .delay(100, TimeUnit.MILLISECONDS)
                }
                .subscribe {
                    showMessage(it)
                }

        Thread.sleep(1000)
    }

    @Test @Throws(Exception::class)
    fun sample10() {
        // 受け取ったデータのflowableが終わってから、次の処理を実行する
        Flowable.just("a", "b", "c")
                .concatMap {
                    Flowable.just(it)
                            .delay(100, TimeUnit.MILLISECONDS)
                }
                .subscribe {
                    showMessage(it)
                }

        Thread.sleep(1000)
    }

    @Test @Throws(Exception::class)
    fun sample11() {
        Observable.just(1, 2, 3, 4)
                .map {
                    showMessage("multiplication" + it.toString())
                    it * 2
                }
                .map {
                    showMessage("addition" + it.toString())
                    it + 5
                }
                .subscribe {
                    showMessage(it.toString())
                }

        Thread.sleep(500)
    }

    @Test @Throws(Exception::class)
    fun sample_doAfterNext() {
        val publishSubject = PublishSubject.create<Int>()

        Observable.just(1)
                .doAfterNext {
                    println("doAfterNext")
                    publishSubject.onNext(it * 2)
                }
                .flatMap {
                    println("receive start")
                    publishSubject
                }
                .subscribe({ println(it) })
    }

    @Test @Throws(Exception::class)
    fun sample_retryWhen() {
        Observable.create<Int> { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onError(HogeException())
            emitter.onComplete()
        }.retryWhen { observable ->
            observable.flatMap { e ->
                val exception = if (e is HogeException) FooException() else e
                Observable.error<Int>(exception)
            }
        }.subscribe({
            println(it)
        }, { e ->
            println(e.javaClass.simpleName)
        }, {})
    }

    class HogeException : RuntimeException()
    class FooException : RuntimeException()

    @Test @Throws(Exception::class)
    fun sample_sample() {
        val delayUploadNotify = PublishSubject.create<Int>()

        delayUploadNotify.sample(1, TimeUnit.SECONDS)
                .subscribe { showMessage(it.toString()) }

        (1..10).map {
            delayUploadNotify.onNext(it)
            Thread.sleep(300)
        }

        Thread.sleep(1000)
    }

    @Test @Throws(Exception::class)
    fun sample_completable_to_observable() {

        Observable.just(1, 2, 3)
                .flatMap { number ->
                    Completable.fromAction {
                        showMessage("completableTest: " + number)
                        Thread.sleep(300)
                    }
                            .toSingleDefault(number)
                            .toObservable()
                }
                .doOnNext {
                    showMessage("doOnNext")
                }
                .doOnComplete {
                    showMessage("doOnComplete")
                }
                .subscribe({
                    showMessage("onNext: " + it.toString())
                }, {
                    it.printStackTrace()
                    showMessage("hoge")
                }, {
                    showMessage("onComplete")
                })
    }

    @Test @Throws(Exception::class)
    fun sample_completable_to_observable2() {

        Observable.just(1, 2, 3)
                .flatMap { number ->
                    Completable.fromAction {
                        showMessage("completableTest: " + number)
                        Thread.sleep(300)
                    }
                            .toObservable<Int>()
                            .single(number)
                            .toObservable()
                }
                .doOnNext {
                    showMessage("doOnNext")
                }
                .doOnComplete {
                    showMessage("doOnComplete")
                }
                .subscribe({
                    showMessage("onNext: " + it.toString())
                }, {
                    it.printStackTrace()
                    showMessage("hoge")
                }, {
                    showMessage("onComplete")
                })
    }

    private fun completableTest(task: Int) =
            Completable.fromAction {
                showMessage("completableTest: " + task.toString())
                Thread.sleep(300)
            }

    @Test @Throws(Exception::class)
    fun sample_single_reduce() {
        val single1 = Single.just(1)
        val single2 = Single.just(2)
        val single3 = Single.just(3)
        val single4 = Single.just(4)


        Single.merge(single1, single2, single3, single4)
                .reduce(0, { t1: Int, t2: Int ->
                    t1 + t2
                })
                .subscribe({
                    showMessage(it.toString())
                }, {
                    it.printStackTrace()
                })

    }

    private fun getStringMutableList(count: Int): MutableList<String> {
        val list = mutableListOf<String>()
        (1..count).mapTo(list) { "hoge" + it }
        return list
    }

    private fun getObserver(tag: String): DisposableObserver<String> {
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

