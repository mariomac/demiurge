#!/usr/bin/env Rscript

args <- commandArgs(trailingOnly = TRUE)

if (length(args) != 6){
    cat("ERROR IN PASSING PARAMETERS: EXPECTED 6")
    stop("ERROR IN PASSING PARAMETERS: EXPECTED 6")
}

library(forecast)

file = args[1]
type = args[2]
NUM_FORECAST = strtoi(args[3])
NOW = strtoi(args[4])
MAX = strtoi(args[5])
file_output = args[6]
#
#file = "/home/mcanuto/BSC/Projects/RenewIT/RES_2015_short.csv"
# type = "green"
# NUM_FORECAST = window #Number of predicted values
# MAX = 100 #Max number of past values to consider
# NOW = 2 #Current time index


FREQUENCY = 1
interval = 1
data <- read.csv(file, check.names=FALSE)

start = NOW*interval - MAX*interval

if (start <0){start = 0}
cat("start: ", start, "\n")
cat("end: ",NOW, "\n")

data <- data[start:NOW,]



data <- data[complete.cases(data), ]

switch(type,
green = { TYPE = "Energia.renovable"},
total = { TYPE = "Energia.total"},
RES = { TYPE = "RES"})

power.ts = data[,TYPE]
cat(power.ts)
sensor<-ts(power.ts,frequency=FREQUENCY)
fit <- auto.arima(sensor)
fcast <- forecast(fit, h = NUM_FORECAST)

fcast.values = c(fcast$mean)

time = seq(from=NOW+FREQUENCY, to=NOW+NUM_FORECAST)
result_file = data.frame(cbind(timestamp = time, value = fcast.values))
#cat(fcast.values, sep = "\n")

write.csv(result_file, file = file_output ,row.names=FALSE)


