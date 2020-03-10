#!/usr/bin/python3
import pandas as pd
import matplotlib.pyplot as plt
data=pd.read_csv("metrics.csv")
print(data)
# plt.errorbar(data['Generation'], data['Average'], yerr=[data['Min'], data['Max']])
plt.plot(data['Generation'], data['Average'])
plt.plot(data['Generation'], data['Max'])
plt.plot(data['Generation'], data['Min'])
plt.show()