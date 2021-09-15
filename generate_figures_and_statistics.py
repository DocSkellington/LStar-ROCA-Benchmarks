#!/bin/env python3

# TODO: add counters for # of closedness and inconsistencies
# Add column for size of alphabet after VCA transformation

import pandas
import numpy as np
from scipy.interpolate import griddata
import matplotlib.pyplot as plt
from matplotlib import cm

df = pandas.read_csv("benchmarks/Results/Random/Random.csv")

print(df.head())

column = "Total time (ms)"
time = df[["Target ROCA size", "Alphabet size", column]]
print(type(time["Target ROCA size"]))
time = time.replace(to_replace="Timeout", value=60*10)
time = time.drop(time[time[column] == "Error"].index)
time[column] = pandas.to_numeric(time[column])

time_for_1_2 = time.loc[(time["Target ROCA size"] == 1) | (time["Target ROCA size"] == 2)]
# To seconds
# time[column] = time[column].transform(lambda x: x / 1000)

print("Timeout:", time[time[column] == "Timeout"])
print("Error:", time[time[column] == "Error"])

dfGrouped = time_for_1_2.groupby(by=["Target ROCA size", "Alphabet size"])
means = dfGrouped.mean()
medians = dfGrouped.median()
print("Mean", means.head())
print("Median", medians)

original_X = [x[0] for x in dfGrouped.groups.keys()]
original_Y = [x[1] for x in dfGrouped.groups.keys()]
print(original_X, original_Y)
X = np.linspace(min(original_X), max(original_X), len(original_X))
Y = np.linspace(min(original_Y), max(original_Y), len(original_Y))
print(X, Y)
X, Y = np.meshgrid(X, Y)
print(X, Y)

Z_mean = griddata((original_X, original_Y), means[column], (X, Y), method="cubic")
Z_mean[X > 2] = np.nan
Z_median = griddata((original_X, original_Y), medians[column], (X, Y), method="cubic")

fig = plt.figure()
ax = fig.add_subplot(projection="3d")
surf_mean = ax.plot_surface(X, Y, Z_mean, cmap=cm.Blues, linewidth=0, antialiased=False)
# surf_median = ax.plot_surface(X, Y, Z_median, cmap=cm.Reds, linewidth=0, antialiased=False)

# ax.set_xlabel("Target ROCA size")
# ax.set_ylabel("Alphabet size")
# ax.set_zlabel("Time (ms)")
# ax.set_title("Mean")
plt.show()
plt.close(fig)

fig, ax = plt.subplots()
