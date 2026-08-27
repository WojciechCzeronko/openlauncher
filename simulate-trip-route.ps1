# Simulates a car driving south along Gdanska Street in Bydgoszcz.
# The route points follow the road from around Gdanska 100 toward Gdanska 20.

$SpeedKmh = 30.0
$UpdateIntervalSeconds = 1.0
$Altitude = 70.0
$Accuracy = 3.0

$SpeedMps = $SpeedKmh / 3.6

# Road anchor points.
$Route = @(
    @{ Lat = 53.133333; Lon = 18.011111 },
    @{ Lat = 53.132800; Lon = 18.010620 },
    @{ Lat = 53.131800; Lon = 18.009650 },
    @{ Lat = 53.130833; Lon = 18.008889 }, # Gdanska 60
    @{ Lat = 53.130200; Lon = 18.008200 },
    @{ Lat = 53.129694; Lon = 18.007639 }, # Gdanska 46
    @{ Lat = 53.129500; Lon = 18.007472 }, # Gdanska 44
    @{ Lat = 53.129278; Lon = 18.007250 }, # Gdanska 42
    @{ Lat = 53.129083; Lon = 18.007083 }, # Gdanska 40
    @{ Lat = 53.128972; Lon = 18.006944 }, # Gdanska 38
    @{ Lat = 53.128500; Lon = 18.006350 },
    @{ Lat = 53.128000; Lon = 18.005780 },
    @{ Lat = 53.127600; Lon = 18.005300 }  # Gdanska 20
)

function Get-DistanceMeters {
    param(
        [double]$Lat1,
        [double]$Lon1,
        [double]$Lat2,
        [double]$Lon2
    )

    $EarthRadius = 6371000.0

    $Lat1Rad = $Lat1 * [Math]::PI / 180.0
    $Lat2Rad = $Lat2 * [Math]::PI / 180.0

    $DeltaLat =
        ($Lat2 - $Lat1) * [Math]::PI / 180.0

    $DeltaLon =
        ($Lon2 - $Lon1) * [Math]::PI / 180.0

    $A =
        [Math]::Sin($DeltaLat / 2.0) *
        [Math]::Sin($DeltaLat / 2.0) +
        [Math]::Cos($Lat1Rad) *
        [Math]::Cos($Lat2Rad) *
        [Math]::Sin($DeltaLon / 2.0) *
        [Math]::Sin($DeltaLon / 2.0)

    $C =
        2.0 *
        [Math]::Atan2(
            [Math]::Sqrt($A),
            [Math]::Sqrt(1.0 - $A)
        )

    return $EarthRadius * $C
}

function Get-BearingDegrees {
    param(
        [double]$Lat1,
        [double]$Lon1,
        [double]$Lat2,
        [double]$Lon2
    )

    $Lat1Rad = $Lat1 * [Math]::PI / 180.0
    $Lat2Rad = $Lat2 * [Math]::PI / 180.0
    $DeltaLon =
        ($Lon2 - $Lon1) * [Math]::PI / 180.0

    $Y =
        [Math]::Sin($DeltaLon) *
        [Math]::Cos($Lat2Rad)

    $X =
        [Math]::Cos($Lat1Rad) *
        [Math]::Sin($Lat2Rad) -
        [Math]::Sin($Lat1Rad) *
        [Math]::Cos($Lat2Rad) *
        [Math]::Cos($DeltaLon)

    $Bearing =
        [Math]::Atan2($Y, $X) *
        180.0 /
        [Math]::PI

    return ($Bearing + 360.0) % 360.0
}

function Send-MockLocation {
    param(
        [double]$Latitude,
        [double]$Longitude,
        [double]$Bearing
    )

    $Lat =
        $Latitude.ToString(
            "F7",
            [Globalization.CultureInfo]::InvariantCulture
        )

    $Lon =
        $Longitude.ToString(
            "F7",
            [Globalization.CultureInfo]::InvariantCulture
        )

    $Speed =
        $SpeedMps.ToString(
            "F2",
            [Globalization.CultureInfo]::InvariantCulture
        )

    $BearingValue =
        $Bearing.ToString(
            "F1",
            [Globalization.CultureInfo]::InvariantCulture
        )

    $AltitudeValue =
        $Altitude.ToString(
            "F1",
            [Globalization.CultureInfo]::InvariantCulture
        )

    $AccuracyValue =
        $Accuracy.ToString(
            "F1",
            [Globalization.CultureInfo]::InvariantCulture
        )

    adb shell am start-foreground-service `
        --user 0 `
        -n io.appium.settings/.LocationService `
        --es longitude $Lon `
        --es latitude $Lat `
        --es altitude $AltitudeValue `
        --es speed $Speed `
        --es bearing $BearingValue `
        --es accuracy $AccuracyValue `
        | Out-Null

    Write-Host (
        "GPS: {0}, {1}  speed={2} km/h  bearing={3}" -f
        $Lat,
        $Lon,
        $SpeedKmh,
        $BearingValue
    )
}

Write-Host "Enabling Appium mock location..."
adb shell appops set `
    io.appium.settings `
    android:mock_location `
    allow

Write-Host ""
Write-Host "Starting simulated trip..."
Write-Host "Speed: $SpeedKmh km/h"
Write-Host ""

$DistancePerUpdate =
    $SpeedMps *
    $UpdateIntervalSeconds
$InitialPoint = $Route[0]

$InitialBearing =
    Get-BearingDegrees `
        $Route[0].Lat `
        $Route[0].Lon `
        $Route[1].Lat `
        $Route[1].Lon

Write-Host ""
Write-Host "Setting initial GPS position..."

Send-MockLocation `
    -Latitude $InitialPoint.Lat `
    -Longitude $InitialPoint.Lon `
    -Bearing $InitialBearing

Write-Host ""
Write-Host "Initial position set."
Write-Host "Now select the destination in OpenLauncher."
Write-Host ""
Read-Host "Press ENTER to start driving"
for (
    $SegmentIndex = 0;
    $SegmentIndex -lt $Route.Count - 1;
    $SegmentIndex++
) {
    $Start = $Route[$SegmentIndex]
    $End = $Route[$SegmentIndex + 1]

    $SegmentDistance =
        Get-DistanceMeters `
            $Start.Lat `
            $Start.Lon `
            $End.Lat `
            $End.Lon

    $Bearing =
        Get-BearingDegrees `
            $Start.Lat `
            $Start.Lon `
            $End.Lat `
            $End.Lon

    $Steps =
        [Math]::Max(
            1,
            [Math]::Ceiling(
                $SegmentDistance /
                $DistancePerUpdate
            )
        )

    for (
        $Step = 0;
        $Step -lt $Steps;
        $Step++
    ) {
        $Progress =
            $Step / $Steps

        $Latitude =
            $Start.Lat +
            ($End.Lat - $Start.Lat) *
            $Progress

        $Longitude =
            $Start.Lon +
            ($End.Lon - $Start.Lon) *
            $Progress

        Send-MockLocation `
            -Latitude $Latitude `
            -Longitude $Longitude `
            -Bearing $Bearing

        Start-Sleep `
            -Milliseconds (
                $UpdateIntervalSeconds *
                1000
            )
    }
}

$FinalPoint =
    $Route[$Route.Count - 1]

Send-MockLocation `
    -Latitude $FinalPoint.Lat `
    -Longitude $FinalPoint.Lon `
    -Bearing 220.0

Write-Host ""
Write-Host "Destination reached."
Write-Host (
    "Final position: {0}, {1}" -f
    $FinalPoint.Lat,
    $FinalPoint.Lon
)