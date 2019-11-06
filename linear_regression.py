import os
os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
import tensorflow as tf
import numpy as np
import random
import sys
import csv

data = np.genfromtxt('train.csv', delimiter = ',')
data = np.delete(data, (0), axis=0)

random.shuffle(data)

#data
x1_data = data[:, [0]].flatten()
x2_data = data[:, [1]].flatten()
x3_data = data[:, [2]].flatten()
x4_data = data[:, [3]].flatten()
#x5_data = data[:, [4]].flatten()
#x6_data = data[:, [5]].flatten()
y_data = data[:, [-1]].flatten()


W1 = tf.Variable(tf.random.uniform([1], -1.0, 1.0))
W2 = tf.Variable(tf.random.uniform([1], -1.0, 1.0))
W3 = tf.Variable(tf.random.uniform([1], -1.0, 1.0))
W4 = tf.Variable(tf.random.uniform([1], -1.0, 1.0))
b  = tf.Variable(tf.random.uniform([1], -1.0, 1.0))

    
# feature 갯수만큼 곱하는 이 부분을 제외하면 one-variable과 다른 곳이 없다
hypothesis = W1*x1_data + W2*x2_data + W3*x3_data + W4*x4_data + b# + W5*x5_data + W6*x6_data + b

cost = tf.reduce_mean(tf.square(hypothesis - y_data))

rate = tf.Variable(0.000001)
optimizer = tf.compat.v1.train.GradientDescentOptimizer(rate)
train = optimizer.minimize(cost)

init = tf.compat.v1.global_variables_initializer()

sess = tf.compat.v1.Session()
sess.run(init)

for step in range(20000):

    sess.run(train)

    #if step%20 == 0:
     #  print(step, sess.run(cost), sess.run(W1), sess.run(W2), sess.run(W3), sess.run(W4), sess.run(b))
print(sess.run(W1))
print(sess.run(W2))
print(sess.run(W3))
print(sess.run(W4))
print(sess.run(b))

w1 = sess.run(W1)
w2 = sess.run(W2)
w3 = sess.run(W3)
w4 = sess.run(W4)
bb = sess.run(b)

# test data

def rmse(p, a):
    return np.sqrt(((p-a)**2).mean())

def rmse_val(predict_result, y):
    return rmse(np.array(predict_result), np.array(y))

y_test = [i[-1] for i in data]
y_temp = [i[0:4] for i in data]
testing_y = []
for i in range(len(y_temp)):
    testing_y.append(int(y_temp[i][0]*w1[0] + y_temp[i][1]*w2[0] + y_temp[i][2]*w3[0] + y_temp[i][3]*w4[0] + bb[0]))

#print(rmse_val(testing_y, y_test))
#print(np.mean(np.equal(testing_y, y_test)))

#print(len(y_test))
#print(len(testing_y))


sess.close()
