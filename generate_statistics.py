#!/bin/env python3

import sys
from collections import defaultdict

import pandas
import numpy as np
from scipy.interpolate import griddata
import matplotlib.pyplot as plt
from matplotlib import cm

def write_surface_data(filepath: str, data_to_write):
    with open(filepath, "w") as f:
        header = " ".join([data[0] for data in data_to_write])
        f.write("X Y " + header + "\n")
        for x in range(len(X)):
            for y in range(len(Y)):
                f.write(
                    f"{X[x][y]} " +
                    f"{Y[x][y]} " +
                    " ".join([str(data[1][x][y]) for data in data_to_write]) +
                    "\n"
                )
            f.write("\n")


def write_table(filepath: str, df: pandas.DataFrame):
    with open(filepath, "w") as f:
        f.write(df.to_latex(index=False))


filepath = sys.argv[1]
timelimit_in_s = int(sys.argv[2])
df = pandas.read_csv(filepath)

# Grouping by input values
dfGrouped = df.groupby(by=["Target ROCA size", "Alphabet size"])

# Meshgrid (for 3D surface plots)
original_X = [x[0] for x in dfGrouped.groups.keys()]
original_Y = [x[1] for x in dfGrouped.groups.keys()]
X = np.linspace(min(original_X), max(original_X), len(original_X))
Y = np.linspace(min(original_Y), max(original_Y), len(original_Y))
X, Y = np.meshgrid(X, Y)

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
X_for_timeouts = [x[0] for x in dfGrouped.groups]
Y_for_timeouts = [x[1] for x in dfGrouped.groups]
timeouts_and_errors = pandas.DataFrame({
    "Target ROCA size": X_for_timeouts,
    "Alphabet size": Y_for_timeouts,
    "Timeouts": number_timeouts,
    "Errors": number_errors
})

write_table("statistics/timeouts_errors.tex", timeouts_and_errors)

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
    "Result target size"
]
numbers = df[["Target ROCA size", "Alphabet size"] + columns]
numbers = numbers.drop(numbers[numbers[columns[0]] == "Timeout"].index)
numbers = numbers.drop(numbers[numbers[columns[0]] == "Error"].index)
for column in columns:
    numbers[column] = pandas.to_numeric(numbers[column])

numbers_grouped = numbers.groupby(by=["Target ROCA size", "Alphabet size"])
numbers_means = numbers_grouped.mean()
numbers_medians = numbers_grouped.median()

# Time columns
columns = [
    "Total time (ms)",
    "ROCA counterexample time (ms)",
    "DFA counterexample time (ms)",
    "Learning ROCA time (ms)",
    "Table time (ms)",
    "Finding descriptions (ms)"
]
time = df[["Target ROCA size", "Alphabet size"] + columns]
time = time.replace(to_replace="Timeout", value=timelimit_in_s * 1000) # * 1000 because we store milliseconds
time = time.drop(time[time[columns[0]] == "Error"].index)
for column in columns:
    time[column] = pandas.to_numeric(time[column])
    # From milliseconds to seconds
    time[column] = time[column].transform(lambda x: x / 1000)

timeGrouped = time.groupby(by=["Target ROCA size", "Alphabet size"])
time_means = timeGrouped.mean()
time_medians = timeGrouped.median()

# We compute the surface data points
Z_all = []

# For timeouts and errors
Z_all.append(("Timeouts", griddata((original_X, original_Y), timeouts_and_errors["Timeouts"], (X, Y), method="cubic")))
Z_all.append(("Errors", griddata((original_X, original_Y), timeouts_and_errors["Errors"], (X, Y), method="cubic")))

# For numbers and time
for newName, column in [
    ("TotalTime", "Total time (ms)"),
    ("ROCACex", "ROCA counterexample time (ms)"),
    ("DFACex", "DFA counterexample time (ms)"),
    ("LearningROCA", "Learning ROCA time (ms)"),
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
    ("HatS", "|? \\ S|"),
    ("Bin", "# of bin rows"),
    ("Target", "Result target size")
]:
    dataframe_means = time_means if "(ms)" in column else numbers_means
    Z_mean = griddata((original_X, original_Y), dataframe_means[column], (X, Y), method="cubic")
    dataframe_medians = time_medians if "(ms)" in column else numbers_medians
    Z_median = griddata((original_X, original_Y), dataframe_medians[column], (X, Y), method="cubic")
    Z_all.append((newName + "Mean", Z_mean))
    Z_all.append((newName + "Median", Z_median))

write_surface_data("statistics/surface_points.dat", Z_all)