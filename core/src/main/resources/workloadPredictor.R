#!/usr/bin/env Rscript

args <- commandArgs(trailingOnly = TRUE)

if (length(args) != 5){
    cat("ERROR IN PASSING PARAMETERS: EXPECTED 5")
    stop("ERROR IN PASSING PARAMETERS: EXPECTED 5")
}

library(forecast)

file = args[1]
NUM_FORECAST = strtoi(args[2])
NOW = strtoi(args[3])
MAX = strtoi(args[4])
file_output = args[5]
#
#file = "/home/mcanuto/BSC/Projects/RenewIT/RES_2015_short.csv"
# type = "green"
# NUM_FORECAST = window #Number of predicted values
# MAX = 100 #Max number of past values to consider
# NOW = 2 #Current time index

FREQUENCY = 1
#cat(args)
data <- read.csv(file, check.names=FALSE)

start = NOW - MAX
if (start <0){start = 0}

data <- data[start:NOW,]
data <- data[complete.cases(data), ]

power.ts = data$power

sensor<- ts(power.ts,frequency=FREQUENCY)
fit <- auto.arima(sensor)
fcast <- forecast(fit, h = NUM_FORECAST)

fcast.values = c(fcast$mean)
time = seq(from=NOW+FREQUENCY, to=NOW+NUM_FORECAST)
result_file = data.frame(cbind(timestamp = time, value = fcast.values))
cat(fcast.values, sep = "\n")

write.csv(result_file, file = file_output ,row.names=FALSE)

