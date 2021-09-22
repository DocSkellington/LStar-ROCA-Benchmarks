#!/bin/env python3

import sys

import pandas
import numpy as np
from scipy.interpolate import griddata
import matplotlib.pyplot as plt
from matplotlib import cm

def save_to_file(filename: str, X, Y, Z):
    with open(filename, "w") as f:
        for y in range(len(Y)):
            for x in range(len(X)):
                f.write(f"{X[x][y]} {Y[x][y]} {Z[x][y]}\n")
            f.write("\n")

filepath = sys.argv[1]

df = pandas.read_csv(filepath)

column = "Total time (ms)"
time = df[["Target ROCA size", "Alphabet size", column]]
time = time.replace(to_replace="Timeout", value=60*10)
time = time.drop(time[time[column] == "Error"].index)
time[column] = pandas.to_numeric(time[column])
time[column] = time[column].transform(lambda x: x / 1000)

dfGrouped = time.groupby(by=["Target ROCA size", "Alphabet size"])
means = dfGrouped.mean()
medians = dfGrouped.median()

original_X = [x[0] for x in dfGrouped.groups.keys()]
original_Y = [x[1] for x in dfGrouped.groups.keys()]
X = np.linspace(min(original_X), max(original_X), len(original_X))
Y = np.linspace(min(original_Y), max(original_Y), len(original_Y))
X, Y = np.meshgrid(X, Y)

Z_mean = griddata((original_X, original_Y), means[column], (X, Y), method="cubic")
Z_median = griddata((original_X, original_Y), medians[column], (X, Y), method="cubic")

save_to_file("mean_total_time.dat", X, Y, Z_mean)
save_to_file("median_total_time.dat", X, Y, Z_median)