/*
 * Copyright 2016 Miguel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miguelbcr.io.rx_billing_service;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

class TargetUi {
  private Object ui;

  TargetUi(Object ui) {
    this.ui = ui;
  }

  void setUi(Object ui) {
    this.ui = ui;
  }

  Activity activity() {
    return fragment() != null ? fragment().getActivity() : (Activity) ui;
  }

  @Nullable Fragment fragment() {
    if (ui instanceof Fragment) {
      return (Fragment) ui;
    }
    return null;
  }

  Context getContext() {
    return fragment() == null ? activity() : fragment().getContext();
  }
}
