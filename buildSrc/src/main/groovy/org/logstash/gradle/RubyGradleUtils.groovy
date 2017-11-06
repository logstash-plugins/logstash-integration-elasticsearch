package org.logstash.gradle

import org.gradle.internal.impldep.org.bouncycastle.util.test.TestFailedException
import org.jruby.Ruby
import org.jruby.embed.ScriptingContainer

final class RubyGradleUtils {

  private final File buildDir

  private final File projectDir

  RubyGradleUtils(File buildDir, File projectDir) {
    this.buildDir = buildDir
    this.projectDir = projectDir
  }

  /**
   * Executes RSpec for a given plugin.
   * @param plugin Plugin to run specs for
   * @param args CLI arguments to pass to rspec
   */
  void rspec(String plugin, Collection<String> args = []) {
    def returnCode = executeJruby { ScriptingContainer jruby ->
      jruby.currentDirectory = "${projectDir}/logstash-${plugin}-elasticsearch".toString()
      jruby.environment.put "BUNDLER_GEMFILE", "${projectDir}/logstash-${plugin}-elasticsearch/Gemfile".toString()
      jruby.environment.put "ES_VERSION", "5.6.4"
      jruby.runScriptlet("require 'bundler/setup'")
      jruby.runScriptlet("require 'rspec/core/runner'")
      jruby.runScriptlet("require 'rspec'")
      jruby.runScriptlet(
        "RSpec::Core::Runner.run([" +
          (["${projectDir}/logstash-${plugin}-elasticsearch".toString() + "/spec", "-fd"] + args)
            .collect({ "'" + it + "'" }).join(',')
          + "])")
    }
    if (returnCode != 0) {
      throw new TestFailedException()
    }
  }

  /**
   * Executes RSpec for a given plugin.
   * @param plugin Plugin to run specs for
   * @param args CLI arguments to pass to rspec
   */
  void rake(String cwd, String task) {
    executeJruby { ScriptingContainer jruby ->
      jruby.currentDirectory = cwd
      jruby.runScriptlet("require 'rake'")
      jruby.runScriptlet(
        "rake = Rake.application\n" +
          "rake.init\n" +
          "rake.load_rakefile\n" +
          "rake['${task}'].invoke"
      )
    }
  }

  /**
   * Executes Closure using a fresh JRuby environment, safely tearing it down afterwards.
   * @param block Closure to run
   */
  Object executeJruby(Closure<?> block) {
    def jruby = new ScriptingContainer()
    def env = jruby.environment
    env.put "GEM_HOME", "${buildDir}/ruby".toString()
    env.put "GEM_SPEC_CACHE", "${buildDir}/cache".toString()
    env.put "GEM_PATH", "${buildDir}/ruby".toString()
    env.put "LOGSTASH_PATH", "${buildDir}/ls".toString()
    env.put "LOGSTASH_SOURCE", "1"
    try {
      return block(jruby)
    } finally {
      jruby.terminate()
      Ruby.clearGlobalRuntime()
    }
  }
}
