/**
 * Testet den DataCursor mit einer zufaelligen batchSize und verschiedenen Startpunkten
 */
@ParameterizedTest
@ValueSource(ints = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60})
void testDataCursor(int minutesPast) throws NightscoutIOException, NightscoutServerException {
    ZonedDateTime latest = ZonedDateTime.now().minus(minutesPast, ChronoUnit.MINUTES);
    ZonedDateTime oldest = ZonedDateTime.parse("2017-01-01T00:00:00.000Z");
    int batchSize = 20 + random.nextInt(100);
    
    // get entries ueber direkten API Aufruf
    List<VaultEntry> expected = api.getEntries(latest, oldest, batchSize);
    
    // Sammle entries in selbem Zeitraum mit DataCursor
    DataCursor cursor = new DataCursor(api, "entries", "dateString", latest, oldest, batchSize / 2);
    List<JsonObject> actual = new ArrayList<>();
    while (cursor.hasNext()) {
        actual.add(cursor.next());
    }
    
    // Ueberpruefe, dass gleich viele Daten gefunden wurden.
    assertEquals(expected.size(), actual.size());
}

/**
 * Testet den GET und POST Endpunkt der "uam" API. 
 */
@Test
void testUnannouncedMeals() throws NightscoutIOException, NightscoutServerException {
    // check that uam plugin is enabled
    assumeTrue(api.isPluginEnabled("uam"));

    // start somewhere in the last month
    long start = System.currentTimeMillis() - (1 + (random.nextInt(30 * 24)) * 60 * 60 * 1000);
    // generate entries every five minutes
    Supplier<Date> dates = createDateSupplier(start, 5 * 60 * 1000);
    // generate 10 random meals
    List<VaultEntry> uams = random.ints(10, 1, 100)
            .mapToObj(i -> new VaultEntry(VaultEntryType.MEAL_MANUAL, dates.get(), i))
            .collect(Collectors.toList());

    // split into three batches
    api.postUnannouncedMeals(uams, this.getClass().getName(), 4);

    SimpleDateFormat format = NSApi.createSimpleDateFormatTreatment();
    GetBuilder builder = api.getUnannouncedMeals()
            .find("created_at").lte(format.format(uams.get(0).getTimestamp()))
            .find("created_at").gte(format.format(uams.get(uams.size() - 1).getTimestamp()))
            .count(100);     // in case there are other random uams from previous tests in this time
    List<VaultEntry> newUams = builder.getUnannouncedMeals();

    // assert all entries are found (or more)
    assertTrue(newUams.size() >= uams.size());
    if (newUams.size() > uams.size()) {
        // if more entries are found test them individually
        for (VaultEntry e : uams) {
            assertTrue(newUams.contains(e));
        }
    } else {
        // else the collections have to be equal
        assertIterableEquals(uams, newUams);
    }
}

/**
 * Creates a supplier that produces dates in descending order.
 *
 * @param start time of first date
 * @param step  time between dates
 */
private Supplier<Date> createDateSupplier(long start, long step) {
    return new Supplier<Date>() {
        long current = start;

        @Override
        public Date get() {
            Date date = TimestampUtils.createCleanTimestamp(new Date(current));
            current -= step;
            return date;
        }
    };
}
