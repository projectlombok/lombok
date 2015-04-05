<#import "freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <h1>Project Lombok - About the authors and everyone that's helped us create Project Lombok.</h1>
    </div>
    <div class="row">
        <div class="credits">
            <div class="committers personList">
                <h3>Regular contributors to Project Lombok:</h3>

                <div class="row text-center">
                    <div class="person col-md-4">
                        <div class="imgCt"><img class="jappeImg" src="images/unknown_person.gif"/></div>
                        <span class="name">Jappe van der Hel</span>
                    </div>
                    <div class="person  col-md-4">
                        <div class="imgCt"><img class="philippImg" src="images/unknown_person.gif"/></div>
                        <span class="name">Philipp Eichhorn</span>
                    </div>
                    <div class="person col-md-4">
                        <div class="imgCt"><img class="reinierImg" src="images/reinier.jpg"/></div>
                        <span class="name">Reinier Zwitserloot</span>
                    </div>
                </div>
                <div class="row text-center">
                    <div class="person col-md-4">
                        <div class="imgCt"><img class="rjImg" src="images/robbertjan.jpg"/></div>
                        <span class="name">Robbert Jan Grootjans</span>
                    </div>
                    <div class="person col-md-4">
                        <div class="imgCt"><img class="roelImg" src="images/roel.jpg"/></div>
                        <span class="name">Roel Spilker</span>
                    </div>
                    <div class="person col-md-4">
                        <div class="imgCt"><img class="sanderImg" src="images/sander.jpg"/></div>
                        <span class="name">Sander Koning</span>
                    </div>
                </div>
            </div>
            <div class="thanks">
                We'd like to thank:
                <ul>
                    <li><strong>Perry Nguyen</strong> (pfn on ##java on freenode) for creating the inspiration for
                        project lombok.
                    </li>
                    <li><strong>Tor Norbye</strong>, <strong>Jan Lahoda</strong>, and <strong>Petr Jiricka</strong> for
                        helping out with Netbeans internals and/or javac.
                    </li>
                    <li><a href="http://javaposse.com/">The Java Posse</a> for making the java community
                        <em>awesome</em>. Listen to their podcast!
                    </li>
                    <li>all contributors who submitted patches or helped answering questions!</li>
                </ul>
                as well as the authors of the following tools that we use:
                <ul>
                    <li><a href="http://code.google.com/">Google Code Hosting</a> for hosting our issue tracker as well
                        as the lombok releases.</a></li>
                    <li><a href="http://github.com/">Github</a> for hosting lombok's repository.</li>
                    <li>The <a href="http://asm.ow2.org/index.html">ASM team</a> at ObjectWeb for creating an excellent
                        class file editing tool. Lombok uses ASM to interact with Eclipse.
                    </li>
                    <li><strong>Markus Gebhard</strong> for creating <a href="http://java2html.de/">java2html</a> which
                        we use for the example code snippets on the features pages.
                    </li>
                    <li><a href="http://camendesign.com/code/video_for_everybody">Kroc Camen</a>'s video for everbody.
                        The lombok demo video
                        runs on just about every system imaginable because of it.</a></li>
                    <li>Longtail Video's <a href="http://www.longtailvideo.com/players/jw-flv-player/">JWPlayer</a>,
                        which is bringing the video to those of you who have an aging browser.
                    </li>
                    <li>The <a href="http://code.google.com/p/spi/">spi project</a>, which makes it very easy to extend
                        lombok with your own transformations.
                    </li>
                    <li><a href="http://ant.apache.org/ivy/">Apache Ivy</a> - Dependency management</li>
                    <li><a href="http://cobertura.sourceforge.net/">Cobertura</a> which we use to ensure our tests cover
                        as much as possible.
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
</@u.page>
