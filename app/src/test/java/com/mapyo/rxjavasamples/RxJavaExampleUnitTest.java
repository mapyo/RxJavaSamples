package com.mapyo.rxjavasamples;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class RxJavaExampleUnitTest {
    @Test
    public void sample1() throws Exception {
        List<String> list = getStringList(5);
        Observable.fromIterable(list)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                });
    }

    private List<String> getStringList(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("hoge" + count);
        }

        return list;
    }
}
