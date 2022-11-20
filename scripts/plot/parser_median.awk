BEGIN {
  FS = ",";
}

/[0-9]+/","{
  threads = $1;
  index_threads = threads - 1;
  sum += $2;
  temp = $2;
  # print "START "
  for (i = 0; i < numThreads[index_threads]; i++) {
    # print "i: '" i "'"
    if (temp < array[index_threads, i]) {
      aux = array[index_threads, i];
      array[index_threads, i] = temp;
      temp = aux;
    }
  }
  array[index_threads, numThreads[index_threads]++] = temp;
  # print $0
}

END {
  pt = 0;
  nt = numThreads[pt];
  if (nt % 2 == 0) {#even
    mi = int((nt / 2) - 1);
    me = array[pt, mi];
    md = array[pt, mi + 1];
    mc = ((me + md) / 2.0);
  } else {#odd
    mi = int(nt / 2);
    mc = array[pt, mi];
  }
  if (speedup == 1) {
    timeSingleThread = mc;
  }
  #print "time 1 thread: " th ", " timeSingleThread;

  for (ti in numThreads) {
    nt = numThreads[ti];
    th = ti + 1;
    #print "ti = " ti ",  nt = " nt;
    if (nt % 2 == 0) {#even
      mi = int((nt / 2) - 1);
      me = array[ti, mi];
      md = array[ti, mi + 1];
      mc = ((me + md) / 2.0);
      #print "EVEN MEDIAN = " ti ", " mc;
    } else {#odd
      mi = int(nt / 2);
      mc = array[ti, mi];
      #print "ODD MEDIAN = " ti ", " mc;
    }
    timeThread = mc;
    if (speedup == 1) {
      if (timeThread != 0) {
        timeThread = timeSingleThread / timeThread;
      } else {
        timeThread = "ERROR";
      }
    }
    #print "nt = " nt ", mi = " mi ", mc = " mc
    print th ", " timeThread;
  }
}
