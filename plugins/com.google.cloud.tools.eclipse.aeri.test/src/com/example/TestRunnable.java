/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

// Simple runnable that creates an Exception instance. Intention is to create a stack trace that
// does not have entries with "com.google.cloud.tools.*" packages.
public class TestRunnable implements Runnable {
  public Throwable exception;

  @Override
  public void run() {
    firstStack();
  }

  private void firstStack() {
    secondStack();
  }

  private void secondStack() {
    exception = new NullPointerException("TESTING");
  }
}
