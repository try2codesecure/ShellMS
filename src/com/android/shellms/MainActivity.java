/*	Copyleft 2013, by Rainer is101024@fhstp.ac.at
 *	University of Applied Sciences St.Pölten - http://www.fhstp.ac.at
 *	This file is part of ShellMS (GPLv3 - https://www.gnu.org/licenses/gpl-3.0.html)
 *  ShellMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  ShellMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package com.android.shellms;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}