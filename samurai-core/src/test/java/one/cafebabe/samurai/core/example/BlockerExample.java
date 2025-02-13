/*
 * Copyright 2003-2012 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.cafebabe.samurai.core.example;

public class BlockerExample {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Thread thread = new AThread(args);
            thread.start();
        }
    }
}

@SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
class AThread extends Thread {
    final Object OBJECT;

    AThread(Object obj) {
        this.OBJECT = obj;
    }

    public void run() {
        while (true) {
            synchronized (OBJECT) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
}
