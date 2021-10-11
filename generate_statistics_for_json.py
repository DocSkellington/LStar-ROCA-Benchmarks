#!/bin/env python3
"""
Copyright (C) 2021 - University of Mons and University Antwerpen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

"""
This file reads a CSV file produced by a JSON benchmarks run and extract statistics.
These statistics are written in a table that can immediately be imported in LaTeX.
"""

import sys
from collections import defaultdict

import pandas
import numpy as np

filepath = sys.argv[1]
timelimit_in_s = int(sys.argv[2])
name = sys.argv[3]
df = pandas.read_csv(filepath)

# Retrieving number of timeouts and errors
column = "Total time (ms)"
n_timeouts = len(df[df[column] == "Timeout"])
n_errors = len(df[df[column] == "Error"])

# Number of queries, sizes of counterexample, of sets, and of learnt ROCA
columns = [
    "Membership queries",
    "Counter value queries",
    "Partial equivalence queries",
    "Equivalence queries",
    "Rounds",
    "Openness",
    "Sigma-inconsistencies",
    "Bottom-inconsistencies",
    "Mismatches",
    "Length longest cex",
    "|R|",
    "|S|",
    "|? \\ S|",
    "# of bin rows",
    "Result alphabet size",
    "Result ROCA size"
]
numbers = df[columns]
numbers = numbers.drop(numbers[numbers[columns[0]] == "Timeout"].index)
numbers = numbers.drop(numbers[numbers[columns[0]] == "Error"].index)
for column in columns:
    numbers[column] = pandas.to_numeric(numbers[column])
numbers["|Ŝ|"] = numbers["|S|"] + numbers["|? \\ S|"]

numbers_mean = numbers.mean()

columns = [
    "Total time (ms)",
]
total_time = df[columns]
total_time = total_time.replace(to_replace="Timeout", value=timelimit_in_s * 1000) # * 1000 because we store milliseconds
total_time = total_time.drop(total_time[total_time[columns[0]] == "Error"].index)
for column in columns:
    total_time[column] = pandas.to_numeric(total_time[column])
    # From milliseconds to seconds
    total_time[column] = total_time[column].transform(lambda x: x / 1000)

total_time_mean = total_time.mean()

columns = [
    "ROCA counterexample time (ms)",
    "DFA counterexample time (ms)",
    "Learning DFA time (ms)",
    "Table time (ms)",
    "Finding descriptions (ms)"
]
time = df[columns]
time = time.drop(time[time[columns[0]] == "Timeout"].index)
time = time.drop(time[time[columns[0]] == "Error"].index)

for column in columns:
    time[column] = pandas.to_numeric(time[column])
    # From milliseconds to seconds
    time[column] = time[column].transform(lambda x: x / 1000)

time_mean = time.mean()

statistics_df = pandas.DataFrame(
    {
        f"TO ({timelimit_in_s}s)": [n_timeouts],
        "MO (16GB)": [n_errors],
        "Time (s)": [total_time_mean["Total time (ms)"]],
        "$\lengthCe$": [numbers_mean["Length longest cex"]],
        "$|R|$": [numbers_mean["|R|"]],
        "$|\widehat{S}|$": [numbers_mean["|Ŝ|"]],
        "$|\\automaton|$": [numbers_mean["Result ROCA size"]],
        "$|\Sigma|$": [numbers_mean["Result alphabet size"]]
    }
)

statistics_df.to_latex(f"statistics/{name}.tex", index=False, escape=False, float_format="%.2f")