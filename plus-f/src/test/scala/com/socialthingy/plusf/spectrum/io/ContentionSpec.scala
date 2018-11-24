package com.socialthingy.plusf.spectrum.io

import com.socialthingy.plusf.spectrum.Model
import com.socialthingy.plusf.z80.Clock
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random.nextInt

class ContentionSpec extends WordSpec with Matchers with TableDrivenPropertyChecks {
  val delays48k = Table(
    ("clock cycle", "delay"),
    (14335, 6),
    (14336, 5),
    (14337, 4),
    (14338, 3),
    (14339, 2),
    (14340, 1),
    (14341, 0),
    (14342, 0),
    (14343, 6),
    (14344, 5),
    (14345, 4),
    (14346, 3),
    (14347, 2),
    (14348, 1),
    (14349, 0),
    (14350, 0),
    (14351, 6),
    (14352, 5),
    (14353, 4),
    (14354, 3),
    (14355, 2),
    (14356, 1),
    (14357, 0),
    (14358, 0),
    (14359, 6),
    (14360, 5),
    (14361, 4),
    (14362, 3),
    (14363, 2),
    (14364, 1),
    (14365, 0),
    (14366, 0),
    (14367, 6),
    (14368, 5),
    (14369, 4),
    (14370, 3),
    (14371, 2),
    (14372, 1),
    (14373, 0),
    (14374, 0),
    (14375, 6),
    (14376, 5),
    (14377, 4),
    (14378, 3),
    (14379, 2),
    (14380, 1),
    (14381, 0),
    (14382, 0),
    (14383, 6),
    (14384, 5),
    (14385, 4),
    (14386, 3),
    (14387, 2),
    (14388, 1),
    (14389, 0),
    (14390, 0),
    (14391, 6),
    (14392, 5),
    (14393, 4),
    (14394, 3),
    (14395, 2),
    (14396, 1),
    (14397, 0),
    (14398, 0),
    (14399, 6),
    (14400, 5),
    (14401, 4),
    (14402, 3),
    (14403, 2),
    (14404, 1),
    (14405, 0),
    (14406, 0),
    (14407, 6),
    (14408, 5),
    (14409, 4),
    (14410, 3),
    (14411, 2),
    (14412, 1),
    (14413, 0),
    (14414, 0),
    (14415, 6),
    (14416, 5),
    (14417, 4),
    (14418, 3),
    (14419, 2),
    (14420, 1),
    (14421, 0),
    (14422, 0),
    (14423, 6),
    (14424, 5),
    (14425, 4),
    (14426, 3),
    (14427, 2),
    (14428, 1),
    (14429, 0),
    (14430, 0),
    (14431, 6),
    (14432, 5),
    (14433, 4),
    (14434, 3),
    (14435, 2),
    (14436, 1),
    (14437, 0),
    (14438, 0),
    (14439, 6),
    (14440, 5),
    (14441, 4),
    (14442, 3),
    (14443, 2),
    (14444, 1),
    (14445, 0),
    (14446, 0),
    (14447, 6),
    (14448, 5),
    (14449, 4),
    (14450, 3),
    (14451, 2),
    (14452, 1),
    (14453, 0),
    (14454, 0),
    (14455, 6),
    (14456, 5),
    (14457, 4),
    (14458, 3),
    (14459, 2),
    (14460, 1),
    (14461, 0),
    (14462, 0),
    (14463, 0),
    (14464, 0),
    (14465, 0),
    (14466, 0),
    (14467, 0),
    (14468, 0),
    (14469, 0),
    (14470, 0),
    (14471, 0),
    (14472, 0),
    (14473, 0),
    (14474, 0),
    (14475, 0),
    (14476, 0),
    (14477, 0),
    (14478, 0),
    (14479, 0),
    (14480, 0),
    (14481, 0),
    (14482, 0),
    (14483, 0),
    (14484, 0),
    (14485, 0),
    (14486, 0),
    (14487, 0),
    (14488, 0),
    (14489, 0),
    (14490, 0),
    (14491, 0),
    (14492, 0),
    (14493, 0),
    (14494, 0),
    (14495, 0),
    (14496, 0),
    (14497, 0),
    (14498, 0),
    (14499, 0),
    (14500, 0),
    (14501, 0),
    (14502, 0),
    (14503, 0),
    (14504, 0),
    (14505, 0),
    (14506, 0),
    (14507, 0),
    (14508, 0),
    (14509, 0),
    (14510, 0),
    (14511, 0),
    (14512, 0),
    (14513, 0),
    (14514, 0),
    (14515, 0),
    (14516, 0),
    (14517, 0),
    (14518, 0),
    (14519, 0),
    (14520, 0),
    (14521, 0),
    (14522, 0),
    (14523, 0),
    (14524, 0),
    (14525, 0),
    (14526, 0),
    (14527, 0),
    (14528, 0),
    (14529, 0),
    (14530, 0),
    (14531, 0),
    (14532, 0),
    (14533, 0),
    (14534, 0),
    (14535, 0),
    (14536, 0),
    (14537, 0),
    (14538, 0),
    (14539, 0),
    (14540, 0),
    (14541, 0),
    (14542, 0),
    (14543, 0),
    (14544, 0),
    (14545, 0),
    (14546, 0),
    (14547, 0),
    (14548, 0),
    (14549, 0),
    (14550, 0),
    (14551, 0),
    (14552, 0),
    (14553, 0),
    (14554, 0),
    (14555, 0),
    (14556, 0),
    (14557, 0),
    (14558, 0),
    (14559, 6)
  )

  forAll(delays48k) { (clockCycle, delay) =>
    "48k memory" when {
      s"contended memory address is accessed and clock cycle is $clockCycle" should {
        s"apply a delay of $delay t-states" in {
          // given
          val clock = new Clock()
          val contentionModel = new ContentionModel48K(clock, Model._48K, new Memory48K())
          clock.tick(clockCycle)

          // when
          contentionModel.applyContention(0x4000 + nextInt(0x4000), 0)

          // then
          clock.getTicks shouldBe (clockCycle + delay)
        }
      }

      s"uncontended memory address is accessed and clock cycle is $clockCycle" should {
        "not apply any delay" in {
          // given
          val clock = new Clock()
          val contentionModel = new ContentionModel48K(clock, Model._48K, new Memory48K())
          clock.tick(clockCycle)

          // when
          contentionModel.applyContention(0x8000 + nextInt(0x4000), 0)

          // then
          clock.getTicks shouldBe clockCycle
        }
      }
    }
  }
}
