BEGIN {
  FS = " ";
  n = 0;
  sum = 0;
}

/Time in secs/{
  sum += $7;
  temp = $7;
  for (i = 0; i < n; i++) {
    if (temp < array[i]) {
      aux = array[i];
      array[i] = temp;
      temp = aux;
    }
  }
  array[n++]=temp;
}

END {
  if (n < 1) {
    exit 1;
  }
  sum = 0;
  for (i = 0; i < n; i++) {
    sum += array[i];
  }
  print threads ", " sum;
}
