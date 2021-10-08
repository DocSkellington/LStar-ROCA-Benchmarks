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
This file reads a CSV file produced by a random benchmarks run over a fixed alphabet size and extract statistics.
These statistics are written in a format that can be used by pgfplots to create 3D surface plots in LaTeX.
"""

import sys
from collections import defaultdict

import pandas
import numpy as np

def write_table(filepath: str, df: pandas.DataFrame):
    with open(filepath, "w") as f:
        f.write(df.to_latex(index=False))


filepath = sys.argv[1]
timelimit_in_s = int(sys.argv[2])
df = pandas.read_csv(filepath)

# Grouping by input values
dfGrouped = df.groupby(by=["Target ROCA size", "Alphabet size"])

# Retrieving number of timeouts and errors
number_timeouts = []
number_errors = []
column = "Total time (ms)"
for (x, y) in dfGrouped.groups:
    group = dfGrouped.get_group((x, y))
    n_timeouts = len(group[group[column] == "Timeout"])
    number_timeouts.append(n_timeouts)
    n_errors = len(group[group[column] == "Error"])
    number_errors.append(n_errors)

# We construct a DataFrame to ease the manipulation
X_for_dataset = [x[0] for x in dfGrouped.groups]
Y_for_dataset = [x[1] for x in dfGrouped.groups]
timeouts_and_errors = pandas.DataFrame({
    "Target ROCA size": X_for_dataset,
    "Alphabet size": Y_for_dataset,
    "Timeouts": number_timeouts,
    "Errors": number_errors
}, index=list(range(1, len(X_for_dataset) + 1)))

write_table("statistics/timeouts_errors_fixed_alphabet_size.tex", timeouts_and_errors)

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
    "Result ROCA size"
]
numbers = df[["Target ROCA size", "Alphabet size"] + columns]
numbers = numbers.drop(numbers[numbers[columns[0]] == "Timeout"].index)
numbers = numbers.drop(numbers[numbers[columns[0]] == "Error"].index)
for column in columns:
    numbers[column] = pandas.to_numeric(numbers[column])

numbers["|Ŝ|"] = numbers["|S|"] + numbers["|? \\ S|"]
columns += "|Ŝ|"

numbers_grouped = numbers.groupby(by=["Target ROCA size", "Alphabet size"])
numbers_means = numbers_grouped.mean()
numbers_medians = numbers_grouped.median()

# Time columns
columns = [
    "Total time (ms)",
]
total_time = df[["Target ROCA size", "Alphabet size"] + columns]
total_time = total_time.replace(to_replace="Timeout", value=timelimit_in_s * 1000) # * 1000 because we store milliseconds
total_time = total_time.drop(total_time[total_time[columns[0]] == "Error"].index)
for column in columns:
    total_time[column] = pandas.to_numeric(total_time[column])
    # From milliseconds to seconds
    total_time[column] = total_time[column].transform(lambda x: x / 1000)

total_time_grouped = total_time.groupby(by=["Target ROCA size", "Alphabet size"])
total_time_means = total_time_grouped.mean()
total_time_medians = total_time_grouped.median()

columns = [
    "ROCA counterexample time (ms)",
    "DFA counterexample time (ms)",
    "Learning DFA time (ms)",
    "Table time (ms)",
    "Finding descriptions (ms)"
]
time = df[["Target ROCA size", "Alphabet size"] + columns]
time = time.drop(time[time[columns[0]] == "Timeout"].index)
time = time.drop(time[time[columns[0]] == "Error"].index)

for column in columns:
    time[column] = pandas.to_numeric(time[column])
    # From milliseconds to seconds
    time[column] = time[column].transform(lambda x: x / 1000)

time_grouped = time.groupby(by=["Target ROCA size", "Alphabet size"])
time_means = time_grouped.mean()
time_medians = time_grouped.median()

# We create one big dataset
full_dataset = pandas.DataFrame({
    "Timeouts": number_timeouts,
    "Errors": number_errors,
    "TotalTimeMean": total_time_means["Total time (ms)"],
    "TotalTimeMedian": total_time_medians["Total time (ms)"],
})

for newName, column in [
    ("ROCACex", "ROCA counterexample time (ms)"),
    ("DFACex", "DFA counterexample time (ms)"),
    ("LearningDFA", "Learning DFA time (ms)"),
    ("Table", "Table time (ms)"),
    ("Descriptions", "Finding descriptions (ms)"),
    ("MQ", "Membership queries"),
    ("CVQ", "Counter value queries"),
    ("PEQ", "Partial equivalence queries"),
    ("EQ", "Equivalence queries"),
    ("Rounds", "Rounds"),
    ("Openness", "Openness"),
    ("Sigma", "Sigma-inconsistencies"),
    ("Bot", "Bottom-inconsistencies"),
    ("Mismatches", "Mismatches"),
    ("LengthCex", "Length longest cex"),
    ("R", "|R|"),
    ("S", "|S|"),
    ("HatS", "|Ŝ|"),
    ("HatSMinusS", "|? \\ S|"),
    ("Bin", "# of bin rows"),
    ("Target", "Result ROCA size")
]:
    dataframe_means = time_means if "(ms)" in column else numbers_means
    dataframe_medians = time_medians if "(ms)" in column else numbers_medians
    full_dataset[newName + "Mean"] = dataframe_means[column]
    full_dataset[newName + "Median"] = dataframe_medians[column]

full_dataset.to_csv("statistics/fixedAlphabetSize.dat")