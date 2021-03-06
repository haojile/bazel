// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.profiler;

import com.google.common.base.Predicate;
import java.util.EnumSet;

/**
 * All possible types of profiler tasks. Each type also defines description and
 * minimum duration in nanoseconds for it to be recorded as separate event and
 * not just be aggregated into the parent event.
 */
public enum ProfilerTask {
  /* WARNING:
   * Add new Tasks at the end (before Unknown) to not break the profiles that people have created!
   * The profile file format uses the ordinal() of this enumeration to identify the task.
   */
  PHASE("build phase marker", -1, 0x336699, 0),
  ACTION("action processing", -1, 0x666699, 0),
  ACTION_BUILDER("parallel builder completion queue", -1, 0xCC3399, 0),
  ACTION_SUBMIT("execution queue submission", -1, 0xCC3399, 0),
  ACTION_CHECK("action dependency checking", 10000000, 0x999933, 0),
  ACTION_EXECUTE("action execution", -1, 0x99CCFF, 0),
  ACTION_LOCK("action resource lock", 10000000, 0xCC9933, 0),
  ACTION_RELEASE("action resource release", 10000000, 0x006666, 0),
  ACTION_GRAPH("action graph dependency", -1, 0x3399FF, 0),
  ACTION_UPDATE("update action information", 10000000, 0x993300, 0),
  ACTION_COMPLETE("complete action execution", -1, 0xCCCC99, 0),
  INFO("general information", -1, 0x000066, 0),
  EXCEPTION("exception", -1, 0xFFCC66, 0),
  CREATE_PACKAGE("package creation", -1, 0x6699CC, 0),
  PACKAGE_VALIDITY_CHECK("package validity check", -1, 0x336699, 0),
  SPAWN("local process spawn", -1, 0x663366, 0),
  REMOTE_EXECUTION("remote action execution", -1, 0x9999CC, 0),
  LOCAL_EXECUTION("local action execution", -1, 0xCCCCCC, 0),
  SCANNER("include scanner", -1, 0x669999, 0),
  // 30 is a good number because the slowest items are stored in a heap, with temporarily
  // one more element, and with 31 items, a heap becomes a complete binary tree
  LOCAL_PARSE("Local parse to prepare for remote execution", 50000000, 0x6699CC, 30),
  UPLOAD_TIME("Remote execution upload time", 50000000, 0x6699CC, 0),
  PROCESS_TIME("Remote execution process wall time", 50000000, 0xF999CC, 0),
  REMOTE_QUEUE("Remote execution queuing time", 50000000, 0xCC6600, 0),
  REMOTE_SETUP("Remote execution setup", 50000000, 0xA999CC, 0),
  FETCH("Remote execution file fetching", 50000000, 0xBB99CC, 0),
  VFS_STAT("VFS stat", 10000000, 0x9999FF, 30, true),
  VFS_DIR("VFS readdir", 10000000, 0x0066CC, 30, true),
  VFS_READLINK("VFS readlink", 10000000, 0x99CCCC, 30, true),
  // TODO(olaola): rename to VFS_DIGEST. This refers to all digest function computations.
  VFS_MD5("VFS md5", 10000000, 0x999999, 30, true),
  VFS_XATTR("VFS xattr", 10000000, 0x9999DD, 30, true),
  VFS_DELETE("VFS delete", 10000000, 0xFFCC00, 0, true),
  VFS_OPEN("VFS open", 10000000, 0x009999, 30, true),
  VFS_READ("VFS read", 10000000, 0x99CC33, 30, true),
  VFS_WRITE("VFS write", 10000000, 0xFF9900, 30, true),
  VFS_GLOB("globbing", -1, 0x999966, 30, true),
  VFS_VMFS_STAT("VMFS stat", 10000000, 0x9999FF, 0, true),
  VFS_VMFS_DIR("VMFS readdir", 10000000, 0x0066CC, 0, true),
  VFS_VMFS_READ("VMFS read", 10000000, 0x99CC33, 0, true),
  WAIT("thread wait", 5000000, 0x66CCCC, 0),
  CONFIGURED_TARGET("configured target creation", -1, 0x663300, 0),
  TRANSITIVE_CLOSURE("transitive closure creation", -1, 0x996600, 0),
  TEST("for testing only", -1, 0x000000, 0),
  SKYFRAME_EVAL("skyframe evaluator", -1, 0xCC9900, 0),
  SKYFUNCTION("skyfunction", -1, 0xCC6600, 0),
  CRITICAL_PATH("critical path", -1, 0x666699, 0),
  CRITICAL_PATH_COMPONENT("critical path component", -1, 0x666699, 0),
  HANDLE_GC_NOTIFICATION("gc notification", -1, 0x996633, 0),
  INCLUSION_LOOKUP("inclusion lookup", -1, 0x000000, 0),
  INCLUSION_PARSE("inclusion parse", -1, 0x000000, 0),
  PROCESS_SCAN("process scan", -1, 0x000000, 0),
  LOOP_OUTPUT_ARTIFACTS("loop output artifacts"),
  LOCATE_RELATIVE("locate relative"),
  CONSTRUCT_INCLUDE_PATHS("construct include paths"),
  PARSE_AND_HINTS_RESULTS("parse and hints results"),
  PROCESS_RESULTS_AND_ENQUEUE("process results and enqueue"),
  SKYLARK_LEXER("Skylark Lexer"),
  SKYLARK_PARSER("Skylark Parser"),
  SKYLARK_USER_FN("Skylark user function call", -1, 0xCC0033, 0),
  SKYLARK_BUILTIN_FN("Skylark builtin function call", -1, 0x990033, 0),
  SKYLARK_USER_COMPILED_FN("Skylark compiled user function call", -1, 0xCC0033, 0),
  UNKNOWN("Unknown event", -1, 0x339966, 0);

  // Size of the ProfilerTask value space.
  public static final int TASK_COUNT = ProfilerTask.values().length;

  /** Human readable description for the task. */
  public final String description;
  /** Threshold for skipping tasks in the profile in nanoseconds, unless --record_full_profiler_data
   *  is used */
  public final long minDuration;
  /** Default color of the task, when rendered in a chart. */
  public final int color;
  /** How many of the slowest instances to keep. If 0, no slowest instance calculation is done. */
  public final int slowestInstancesCount;
  /** True if the metric records VFS operations */
  private final boolean vfs;

  ProfilerTask(String description, long minDuration, int color, int slowestInstanceCount) {
    this(description, minDuration, color, slowestInstanceCount, /*vfs=*/ false);
  }

  ProfilerTask(String description, long minDuration, int color, int slowestInstanceCount,
      boolean vfs) {
    this.description = description;
    this.minDuration = minDuration;
    this.color = color;
    this.slowestInstancesCount = slowestInstanceCount;
    this.vfs = vfs;
  }

  ProfilerTask(String description) {
    this(description, -1, 0x000000, 0);
  }

  /** Whether the Profiler collects the slowest instances of this task. */
  public boolean collectsSlowestInstances() {
    return slowestInstancesCount > 0;
  }

  /**
   * Build a set containing all ProfilerTasks for which the given predicate is true.
   */
  public static EnumSet<ProfilerTask> allSatisfying(Predicate<ProfilerTask> predicate) {
    EnumSet<ProfilerTask> set = EnumSet.noneOf(ProfilerTask.class);
    for (ProfilerTask taskType : values()) {
      if (predicate.apply(taskType)) {
        set.add(taskType);
      }
    }
    return set;
  }

  public boolean isVfs() {
    return vfs;
  }
}
