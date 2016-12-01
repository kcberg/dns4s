/*
 * Copyright 2014-2015 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt._
import sbt.Keys._
import sbtrelease._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.osgi.SbtOsgi._

object Build extends sbt.Build {
  lazy val scalaVersions = "2.11.8" :: "2.10.6" :: Nil
  lazy val akkaVersion = "[2.4.0,2.5.0["
  lazy val scalaTestVersion = "3.+"

  def projectSettings(n: String, d: String) = Seq(
    name := n,
    description := d,
    organization := "com.github.mkroli",
    scalaVersion := scalaVersions.head,
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-target:jvm-1.8"),
    crossScalaVersions := scalaVersions,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/mkroli/dns4s")),
    pomExtra := (
      <scm>
        <url>git@github.com:mkroli/dns4s.git</url>
        <connection>scm:git:git@github.com:mkroli/dns4s.git</connection>
      </scm>),
    exportJars := true)

  def projectOsgiSettings(bundleName: String, packagesPrefix: String, packages: String*) = osgiSettings ++ Seq(
    OsgiKeys.exportPackage := packages.map(pkg => packagesPrefix :: (if (pkg.isEmpty) Nil else pkg :: "*" :: Nil) mkString "."),
    OsgiKeys.privatePackage := Nil,
    OsgiKeys.additionalHeaders += "Bundle-Name" -> bundleName)

  lazy val dns4sProjectSettings = Seq(
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "[15.+,18.+]",
      "com.google.code.findbugs" % "jsr305" % "+" % "provided",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.2" % "test"))

  lazy val dns4sAkkaProjectSettings = Seq(
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "[15.+,18.+]",
      "com.google.code.findbugs" % "jsr305" % "+" % "provided",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"))

  lazy val dns4sNettyProjectSettings = Seq(
    libraryDependencies ++= Seq(
      "io.netty" % "netty-handler" % "4.0.+",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"))

  lazy val projectReleaseSettings = Seq(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion))

  lazy val parentSettings = Seq(
    publishArtifact := false)

  lazy val dns4sRoot = Project(
    id = "dns4s",
    base = file("."),
    settings = Defaults.defaultSettings ++
      projectSettings("dns4s", "Scala DNS implementation") ++
      projectReleaseSettings ++
      parentSettings)
    .aggregate(dns4sCore, dns4sAkka, dns4sNetty)

  lazy val dns4sCore = Project(
    id = "dns4s-core",
    base = file("core"),
    settings = Defaults.defaultSettings ++
      projectOsgiSettings("dns4s-core", "com.github.mkroli.dns4s", "", "dsl", "section") ++
      projectSettings("dns4s-core", "Scala DNS implementation") ++
      dns4sProjectSettings)

  lazy val dns4sAkka = Project(
    id = "dns4s-akka",
    base = file("akka"),
    settings = Defaults.defaultSettings ++
      projectOsgiSettings("dns4s-akka", "com.github.mkroli.dns4s", "akka") ++
      projectSettings("dns4s-akka", "Scala DNS implementation - Akka extension") ++
      dns4sAkkaProjectSettings)
    .dependsOn(dns4sCore)

  lazy val dns4sNetty = Project(
    id = "dns4s-netty",
    base = file("netty"),
    settings = Defaults.defaultSettings ++
      projectOsgiSettings("dns4s-netty", "com.github.mkroli.dns4s", "netty") ++
      projectSettings("dns4s-netty", "Scala DNS implementation - Netty extension") ++
      dns4sNettyProjectSettings)
    .dependsOn(dns4sCore)
}
