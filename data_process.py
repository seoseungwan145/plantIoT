import csv

f = open('output.csv', 'r', encoding='utf-8')
ff = open('train.csv', 'a', encoding='utf-8', newline='')
rdr = list(csv.reader(f))

wr = csv.writer(ff)
#print(rdr[40][0])
#print(rdr[40][1])
#print(rdr[40][2])
#print(rdr[40][3])
#print(rdr[40][4])
#print(rdr[40][5])

#wr.writerow(["PM10", "Temperature", "Humidity", "Light", "Later_PM10"])

a = 0

for line in rdr:
    if a+60 >= len(rdr):
        break;
    go1 = (float(rdr[a][3]) + float(rdr[a+10][3]) + float(rdr[a+20][3]) + float(rdr[a+30][3]) + float(rdr[a+40][3]) + float(rdr[a+50][3]) + float(rdr[a+60][3])) / 7
    sum2 = (float(rdr[a][4]) + float(rdr[a+10][4]) + float(rdr[a+20][4]) + float(rdr[a+30][4]) + float(rdr[a+40][4]) + float(rdr[a+50][4]) + float(rdr[a+60][4])) / 7
    hi3 = (float(rdr[a][5]) + float(rdr[a+10][5]) + float(rdr[a+20][5]) + float(rdr[a+30][5]) + float(rdr[a+40][5]) + float(rdr[a+50][5]) + float(rdr[a+60][5])) / 7
    del line[0]
    del line[0]
    del line[1]
    del line[1]
    del line[1]
    line.append(str(round(go1, 2)))
    line.append(str(round(sum2, 2)))
    line.append(str(round(hi3, 2)))
    line.append(rdr[a+60][2])
    #print(line)
    wr.writerow(line)
    a = a + 1

f.close()
ff.close()
