for file in $(find data -regex ".*test[0-9]*"); do
  java -jar build/libs/virt-mem-1.0-SNAPSHOT.jar $file
  cmp -s $file.a $file.out || exit 1
done
