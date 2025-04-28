test:
	mvn test

clean:
	mvn clean

veryclean: clean
	rm -rf test-data

test-data:
	mkdir test-data
	(cd test-data; curl -L https://github.com/max-mapper/csv-spectrum/archive/refs/heads/master.tar.gz | tar -zxvf -)
	(cd test-data; curl -L https://github.com/sineemore/csv-test-data/archive/refs/heads/master.tar.gz | tar -zxvf -)