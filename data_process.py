import csv

f = open('output.csv', 'r', encoding='utf-8')
ff = open('train.csv', 'w', encoding='utf-8', newline='')
rdr = list(csv.reader(f))

wr = csv.writer(ff)

wr.writerow(["PM2.5", "Temperature", "Humidity", "Light", "Later_PM10"])

a = 0
print(len(rdr))


for line in rdr:
    if a+6 >= len(rdr):
        break;
    del line[0]
    del line[3]
    line.append(rdr[a+6][2])
    print(line)
    wr.writerow(line)
    a = a + 1

f.close()
ff.close()
